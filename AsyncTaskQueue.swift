actor TaskQueue{
    private class PendingTask{
        let tag:String?
        init(tag: String?) {
            self.tag = tag
        }
    }
    private class AsyncTask : PendingTask {
        let continuation: CheckedContinuation<Any,Error>?
        let block: () async throws -> Any
        
        init(tag: String?, continuation: CheckedContinuation<Any, Error>?, block: @escaping () async throws -> Any) {
            self.continuation = continuation
            self.block = block
            super.init(tag: tag)
        }
    }
    
    private class StreamTask : PendingTask{
        let continuation: AsyncThrowingStream<Any,Error>.Continuation
        let block: (AsyncThrowingStream<Any,Error>.Continuation) -> Void
        
        init(tag: String?, continuation: AsyncThrowingStream<Any,Error>.Continuation, block: @escaping (AsyncThrowingStream<Any,Error>.Continuation) -> Void) {
            self.continuation = continuation
            self.block = block
            super.init(tag: tag)
        }
    }
    
    let tag:String?
    
    private var pendingTasksContinuation: AsyncStream<PendingTask>.Continuation?
    
    private var pendingTasks: AsyncStream<PendingTask>?
    
    private var scope: Task<Void,Never>?
    
    func initPendingTasks()
    {
        pendingTasks = AsyncStream{ continuation in
            pendingTasksContinuation = continuation
        }
    }
    
    func initScope()
    {
        scope = Task{
            guard let pendingTasks = pendingTasks else { return }
            for await pendingTask in pendingTasks
            {
                log.debug("PendingTask \(pendingTask.tag ?? "") received",source: tag)
                if(Task.isCancelled){ break }
                if let task = pendingTask as? AsyncTask
                {
                    do{
                        log.debug("AsyncTask \(pendingTask.tag ?? "") start",source: tag)
                        let result = try await task.block()
                        log.debug("AsyncTask \(pendingTask.tag ?? "") resume",source: tag)
                        task.continuation?.resume(returning: result)
                    }
                    catch
                    {
                        log.debug("AsyncTask \(pendingTask.tag ?? "") error \(error)",source: tag)
                        task.continuation?.resume(throwing: error)
                    }
                }
                else if let task = pendingTask as? StreamTask
                {
                    do
                    {
                        log.debug("StreamTask \(pendingTask.tag ?? "") start",source: tag)
                        for try await value in AsyncThrowingStream(Any.self, task.block)
                        {
                            log.debug("StreamTask \(pendingTask.tag ?? "") yield",source: tag)
                            task.continuation.yield(value)
                        }
                        log.debug("StreamTask \(pendingTask.tag ?? "") finish",source: tag)
                        task.continuation.finish()
                    }
                    catch
                    {
                        log.debug("StreamTask \(pendingTask.tag ?? "") error \(error)",source: tag)
                        task.continuation.finish(throwing: error)
                    }
                    
                }
                else
                {
                    log.debug("PendingTask discard \(pendingTask)",source: tag)
                }
                if(Task.isCancelled){ break }
            }
        }
    }
    
    init(tag: String?) {
        self.tag = tag ?? "TaskQueue"
        Task{
            await initPendingTasks()
            await initScope()
        }
    }
    
    private func setBlocksContinuation(continuation: AsyncStream<PendingTask>.Continuation)
    {
        pendingTasksContinuation = continuation
    }
    
    func close()
    {
        if(scope?.isCancelled == false)
        {
            scope?.cancel()
        }
    }
    
    func dispatchAndWait<T>(tag:String?=nil,block: @escaping () async throws -> T) async throws -> T
    {
        return (try await withCheckedThrowingContinuation({ continuation in
            pendingTasksContinuation?.yield(AsyncTask(tag: tag, continuation: continuation, block: block))
        })) as! T
    }
    
    func dispatchStream<T>(
        tag:String?=nil,
        block:@escaping (AsyncThrowingStream<T,Error>.Continuation) -> Void) -> AsyncThrowingStream<T,Error>
    {
        let anyStream = AsyncThrowingStream<Any,Error> { continuation in
            pendingTasksContinuation?.yield(StreamTask(tag: tag, continuation: continuation, block: { anyContinuation in
                Task{
                    do
                    {
                        for try await element in AsyncThrowingStream(T.self,block)
                        {
                            anyContinuation.yield(element)
                        }
                        anyContinuation.finish()
                    }
                    catch
                    {
                        anyContinuation.finish(throwing: error)
                    }
                }
            }))
        }
        return AsyncThrowingStream<T,Error> { typedContinuation in
            Task
            {
                do{
                    for try await element in anyStream
                    {
                        typedContinuation.yield(element as! T)
                    }
                    typedContinuation.finish()
                }
                catch
                {
                    typedContinuation.finish(throwing: error)
                }
            }
        }
    }
}
