import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


fun Activity.alert(title:String,message:String?,button:String,handler:(()->Unit)? = null){
    SystemHelper.alert(this,title,message,button,handler)
}
fun Activity.prompt(title:String,message:String?,positive:String,negative:String,inputType:Int,handler:((String)->Unit)? = null){
    SystemHelper.prompt(this,title,message,positive,negative,inputType,handler)
}
fun Activity.confirm(title:String,message:String?, positive:String,negative:String,handler:((Boolean)->Unit)? = null){
    SystemHelper.confirm(this,title,message,positive,negative,handler)
}
fun Activity.picker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: Int, handler: ((Int)->Unit)? = null){
    SystemHelper.picker(this,title,positive,negative,options,selected,handler)
}
fun Activity.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null){

    MaterialAlertDialogBuilder(this).setTitle(title)
            .setMultiChoiceItems(options,selected){ dialog: DialogInterface?, which: Int, isChecked: Boolean ->
                selected[which] = isChecked

            }.setNegativeButton(negative){dialog: DialogInterface?, which: Int ->  
                
            }.setPositiveButton(positive){dialog: DialogInterface?, which: Int ->  
                handler?.invoke(selected)
            }.create().show()
}
fun Activity.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null)
{
    MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setItems(options) { dialog, which ->
                handler?.invoke(which)
            }
            .show()
}


fun androidx.fragment.app.Fragment.alert(title:String, message:String?, button:String, handler:(()->Unit)? = null){
    this.activity?.alert(title,message,button,handler)
}
fun androidx.fragment.app.Fragment.prompt(title:String, message:String?, positive:String, negative:String, inputType:Int, handler:((String)->Unit)? = null){
    this.activity?.prompt(title,message,positive,negative,inputType,handler)
}
fun androidx.fragment.app.Fragment.confirm(title:String, message:String?, positive:String, negative:String, handler:((Boolean)->Unit)? = null) {
    this.activity?.confirm(title,message,positive,negative,handler)
}
fun androidx.fragment.app.Fragment.picker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: Int, handler: ((Int)->Unit)? = null) {
    this.activity?.picker(title,positive,negative,options,selected,handler)
}
fun androidx.fragment.app.Fragment.checker(title: String, positive: String, negative: String, options: Array<CharSequence>, selected: BooleanArray, handler: ((BooleanArray) -> Unit)? = null){
    this.activity?.checker(title,positive,negative,options,selected,handler)
}
fun androidx.fragment.app.Fragment.simplePicker(title: String, options: Array<CharSequence>, handler: ((Int)->Unit)? = null)
{
    this.activity?.simplePicker(title,options,handler)
}
