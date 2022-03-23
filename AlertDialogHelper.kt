import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


fun AppCompatActivity.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null)
{
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setCancelable(true)
            .setItems(options) { dialog, which ->
                handler?.invoke(which)
            }
            .show()
}
fun Fragment.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null) = (this.activity as? AppCompatActivity)?.simplePicker(title, options, handler)

fun AppCompatActivity.alert(title:String, message:String?, button:String, handler:(()->Unit)? = null) {
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(button) { _, _ -> handler?.invoke() }
            .show()
}

fun Fragment.alert(title:String, message:String?, button:String, handler:(()->Unit)? = null) = (this.activity as? AppCompatActivity)?.alert(title,message,button,handler)

fun AppCompatActivity.confirm(title:String,message:String?, positive:String,negative:String,handler:((Boolean)->Unit)? = null) {
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ -> handler?.invoke(true) }
            .setNegativeButton(negative) { _, _ -> handler?.invoke(false) }
            .show()
}
fun Fragment.confirm(title:String,message:String?, positive:String,negative:String,handler:((Boolean)->Unit)? = null) = (this.activity as? AppCompatActivity)?.confirm(title,message,positive,negative,handler)

/**
 * Use with prompt_dialog.xml
 */
fun AppCompatActivity.prompt(title:String,message:String?,positive:String,negative:String,inputType:Int,handler:((String)->Unit)? = null){
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
fun Fragment.prompt(title:String,message:String?,positive:String,negative:String,inputType:Int,handler:((String)->Unit)? = null) = (this.activity as? AppCompatActivity)?.prompt(title,message,positive,negative,inputType,handler)

fun AppCompatActivity.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null){

    MaterialAlertDialogBuilder(this).setTitle(title)
            .setMultiChoiceItems(options,selected){ dialog: DialogInterface?, which: Int, isChecked: Boolean ->
                selected[which] = isChecked

            }.setNegativeButton(negative){dialog: DialogInterface?, which: Int ->  
                
            }.setPositiveButton(positive){dialog: DialogInterface?, which: Int ->  
                handler?.invoke(selected)
            }.create().show()
}
fun Fragment.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null) = (this.activity as? AppCompatActivity)?.checker(title,positive,negative,options,selected,handler)
