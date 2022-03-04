import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


fun Context.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null)
{
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setCancelable(true)
            .setItems(options) { dialog, which ->
                handler?.invoke(which)
            }
            .show()
}
fun Fragment.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null) = this.context?.simplePicker(title, options, handler)

fun Context.alert(title:String, message:String?, button:String, handler:(()->Unit)? = null) {
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(button) { _, _ -> handler?.invoke() }
            .show()
}

fun Fragment.alert(title:String, message:String?, button:String, handler:(()->Unit)? = null) = this.context?.alert(title,message,button,handler)

fun Context.confirm(title:String,message:String?, positive:String,negative:String,handler:((Boolean)->Unit)? = null) {
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ -> handler?.invoke(true) }
            .setNegativeButton(negative) { _, _ -> handler?.invoke(false) }
            .show()
}
fun Fragment.confirm(title:String,message:String?, positive:String,negative:String,handler:((Boolean)->Unit)? = null) = this.context?.confirm(title,message,positive,negative,handler)

/**
 * Use with prompt_dialog.xml
 */
fun Context.prompt(title:String,message:String?,positive:String,negative:String,inputType:Int,handler:((String)->Unit)? = null){
    LayoutInflater.from(this).also { inflater ->
        val binding = PromptDialogBinding.inflate(inflater,null,false)
        val v = binding.root
        binding.textField.editText?.inputType = inputType
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(v)
            .setPositiveButton(positive) { _, _ -> handler?.invoke(binding.textField.editText?.text?.toString() ?: "") }
            .setNegativeButton(negative) { _, _ ->  }
            .show()

    }
}
fun Fragment.prompt(title:String,message:String?,positive:String,negative:String,inputType:Int,handler:((String)->Unit)? = null) = this.context?.prompt(title,message,positive,negative,inputType,handler)

fun Context.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null){

    MaterialAlertDialogBuilder(this).setTitle(title)
            .setMultiChoiceItems(options,selected){ dialog: DialogInterface?, which: Int, isChecked: Boolean ->
                selected[which] = isChecked

            }.setNegativeButton(negative){dialog: DialogInterface?, which: Int ->  
                
            }.setPositiveButton(positive){dialog: DialogInterface?, which: Int ->  
                handler?.invoke(selected)
            }.create().show()
}
fun Fragment.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null) = this.context?.checker(title,positive,negative,options,selected,handler)
