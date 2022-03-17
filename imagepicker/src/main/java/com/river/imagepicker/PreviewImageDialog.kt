package com.river.imagepicker

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.river.imagepicker.adapter.PreviewImageAdapter
import com.river.imagepicker.callback.AnimatorListener
import com.river.imagepicker.callback.OnPageChangeListener
import com.river.imagepicker.entry.LocalMedia
import java.io.Serializable
import kotlin.math.absoluteValue

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class PreviewImageDialog : DialogFragment() {
    private lateinit var actionBack: ImageView
    private lateinit var viewpager: ViewPager
    private lateinit var name: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var checkBoxContainer: FrameLayout
    private lateinit var title: TextView

    var listener: ((selected: List<LocalMedia>) -> Unit)? = null

    private var defaultPosition = 0
    private lateinit var data: List<LocalMedia>
    private lateinit var selectedList: MutableList<LocalMedia>
    private var maxSelectedCount = 0

    private var velocityTracker: VelocityTracker? = null


    @SuppressLint("ResourceAsColor")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            decorView.setPadding(0, 0, 0, 0)
            val lp = attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.dimAmount = 1F
            attributes = lp
            setBackgroundDrawable(ColorDrawable(R.color.black))
        }

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            defaultPosition = getInt("defaultPosition", 0)
            data = getSerializable("data") as List<LocalMedia>? ?: emptyList()
            val temp = getSerializable("selectedList") as MutableList<LocalMedia>? ?: mutableListOf()
            selectedList = data.filter {allItem-> temp.any {selectedItem ->  selectedItem.id == allItem.id} }.toMutableList()
            maxSelectedCount = getInt("maxSelectedCount", Int.MAX_VALUE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_preview_image, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
        actionBack = view.findViewById(R.id.action_back)
        viewpager = view.findViewById(R.id.viewpager)
        name = view.findViewById(R.id.name)
        checkBox = view.findViewById(R.id.checkbox)
        checkBoxContainer = view.findViewById(R.id.checkbox_container)

        title.text = ImagePicker.preview
        name.text = data[defaultPosition].name
        checkBox.isChecked = isChecked(defaultPosition)

        viewpager.adapter = PreviewImageAdapter(requireContext(), data)
        viewpager.addOnPageChangeListener(object : OnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                name.text = data[position].name
                checkBox.isChecked = isChecked(position)
            }
        })

        viewpager.currentItem = defaultPosition

        actionBack.setOnClickListener { dismiss() }

        viewpager.setOnTouchListener(this::onTouch)

        checkBoxContainer.setOnClickListener(this::handleCheck)

        velocityTracker = VelocityTracker.obtain()
    }

    private fun isChecked(position: Int): Boolean {
        return selectedList.contains(data[position])
    }

    private fun handleCheck(view: View) {
        val item = data[viewpager.currentItem]

        if (!checkBox.isChecked) {
            if (selectedList.size >= maxSelectedCount) {
                Toast.makeText(context, String.format(ImagePicker.outMaxSelectedTip, maxSelectedCount), Toast.LENGTH_SHORT).show()
                return
            }
            selectedList.add(item)
            checkBox.isChecked = true
        } else {
            selectedList.remove(item)
            checkBox.isChecked = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.invoke(selectedList)
    }

    override fun onDestroy() {
        velocityTracker?.recycle()
        super.onDestroy()
    }

    private var startX = 0F
    private var startY = 0F
    private var disX = 0F
    private var disY = 0F

    private fun onTouch(view: View, event: MotionEvent): Boolean {
        var isCost = false

        velocityTracker?.addMovement(event)


        if (event.action == MotionEvent.ACTION_DOWN) {
            startX = event.rawX
            startY = event.rawY
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            disX = event.rawX - startX
            disY = event.rawY - startY

            if (allowHandleTouchEvent()) {
                isCost = true

                val screenWidth = requireContext().resources.displayMetrics.widthPixels

                val alpha = Math.max(1F - disY.absoluteValue / screenWidth.toFloat(), 0F)

                updateUI(disY, alpha, alpha)
            }
        } else if (event.action == MotionEvent.ACTION_UP && allowHandleTouchEvent()) {
            velocityTracker?.computeCurrentVelocity(1000)
            val yVelocity = velocityTracker?.yVelocity ?: 0F

            if (viewpager.scaleX < 0.2 || (yVelocity.absoluteValue > 2000F && disY.absoluteValue > 100F)) {
                close()
            } else {
                reset()
            }
        }

        return isCost
    }

    private fun allowHandleTouchEvent() =
        disY.absoluteValue > disX.absoluteValue && disY.absoluteValue >= ViewConfiguration.get(
            context
        ).scaledTouchSlop

    private fun reset() {
        val originAlpha = dialog?.window?.attributes?.dimAmount ?: 1F
        val originTranslationY = viewpager.translationY
        val originScale = viewpager.scaleX

        ValueAnimator.ofFloat(0F, 1F).apply {
            duration = 300L

            addUpdateListener {
                val translationY = originTranslationY * (1F - it.animatedValue as Float)
                val scale = originScale + (1F - originScale) * (it.animatedValue as Float)
                val alpha = originAlpha + (1F - originAlpha) * (it.animatedValue as Float)
                updateUI(translationY, scale, alpha)
            }

            start()
        }
    }

    private fun close() {
        val originAlpha = dialog?.window?.attributes?.dimAmount ?: 1F
        val originTranslationY = viewpager.translationY
        val translationYFlag = if (originTranslationY > 0) 1 else -1
        val originScale = viewpager.scaleX
        val screenWidth = resources.displayMetrics.widthPixels

        ValueAnimator.ofFloat(0F, 1F).apply {
            duration = 300L

            addUpdateListener {
                val translationY =
                    viewpager.translationY + (screenWidth - originTranslationY.absoluteValue) * (it.animatedValue as Float) * translationYFlag
                val scale = originScale * (1F - it.animatedValue as Float)
                val alpha = originAlpha * (1F - it.animatedValue as Float)
                updateUI(translationY, scale, alpha)
            }


            addListener(object : AnimatorListener() {
                override fun onAnimationEnd(p0: Animator?) {
                    dismiss()
                }
            })

            start()
        }
    }

    private fun updateUI(translationY: Float, scale: Float, alpha: Float) {
        viewpager.translationY = translationY
        viewpager.scaleX = scale
        viewpager.scaleY = scale

        dialog?.window?.apply {
            val lp = attributes
            lp.dimAmount = alpha
            attributes = lp
        }
    }

    fun getSelectedList() = selectedList

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            data: List<LocalMedia>,
            selected: List<LocalMedia>,
            maxSelectedCount: Int = Int.MAX_VALUE,
            defaultPosition: Int = 0,
            selectedChangeListener: ((selected: List<LocalMedia>) -> Unit)? = null
        ) {
            val dialog = PreviewImageDialog()

            dialog.listener = selectedChangeListener

            val bundle = Bundle()
            bundle.putSerializable("data", data as Serializable)
            bundle.putInt("defaultPosition", defaultPosition)
            bundle.putSerializable("selectedList", selected as Serializable)
            bundle.putInt("maxSelectedCount", maxSelectedCount)

            dialog.arguments = bundle

            dialog.show(fragmentManager, "PreviewImageDialog")
        }
    }
}