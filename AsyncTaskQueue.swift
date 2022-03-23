class TaskQueue{
    
    private actor TaskQueueActor{
        private var blocks : [(() async -> Any, CheckedContinuation<Any,Never>?)] = []
        private var currentTask : Task<Void,Never>? = nil
        
        func addBlock(block:@escaping () async -> Any, continuation:  CheckedContinuation<Any,Never>? = nil){
            blocks.append((block,continuation))
            next()
        }
        
        func next()
        {
            if(currentTask != nil) {
                return
            }
            if(!blocks.isEmpty)
            {
                let (block,continuation) = blocks.removeFirst()
                currentTask = Task{
                    let result = await block()
                    continuation?.resume(returning: result)
                    currentTask = nil
                    next()
                }
            }
        }
    }
    private let taskQueueActor = TaskQueueActor()
    
    func dispatch(block:@escaping () async ->Void){
        Task{
            await taskQueueActor.addBlock(block: block,continuation: nil)
        }
    }
    
    func dispatchAndWait<T>(block:@escaping() async -> T) async -> T?{
        if let result = await withCheckedContinuation({ continuation in
            Task{
                await taskQueueActor.addBlock(block: block, continuation: continuation)
            }
        }) as? T
        {
            return result
        }
        return nil
    }
    
}