/*
 * NOTICE
 *
 * This is the copyright work of The MITRE Corporation, and was produced for
 * the U. S. Government under Contract Number DTFAWA-10-C-00080, and is subject
 * to Federal Aviation Administration Acquisition Management System Clause
 * 3.5-13, Rights In Data-General, Alt. III and Alt. IV (Oct. 1996).  No other
 * use other than that granted to the U. S. Government, or to those acting on
 * behalf of the U. S. Government, under that Clause is authorized without the
 * express written permission of The MITRE Corporation. For further information,
 * please contact The MITRE Corporation, Contracts Office,7515 Colshire Drive,
 * McLean, VA  22102-7539, (703) 983-6000.
 *
 * Approved for Public Release; Distribution Unlimited. Case Number 14-4045
 */

package com.shaw.ggstart.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.shaw.ggstart.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by KLONG on 7/2/2014.
 * android版的SegmentedControlView
 */
public class SegmentedControlView extends RadioGroup {

    private final static int ITEM_LEFT = 10001;
    private final static int ITEM_RIGHT = 10002;
    private final static int ITEM_MIDDLE = 10003;

    //Helpers
    private int mSdk;
    private Context mCtx;

    //Interaction
    private OnSelectionChangedListener mListener;

    //UI
    private int selectedColor = Color.parseColor("#0099CC");
    private int unselectedColor = Color.TRANSPARENT;
    private int unselectedTextColor = Color.parseColor("#0099CC");
    private int defaultSelection = -1;
    private boolean stretch = false;
    private int selectedTextColor = Color.WHITE;
    private boolean equalWidth = false;
    private int mRadius;
    private int mStroke;
    private float mTextSize;
    private String identifier = "";
    private ColorStateList textColorStateList;

    //Item organization
    private LinkedHashMap<String, String> itemMap = new LinkedHashMap<String, String>();
    private ArrayList<RadioButton> options;

    public SegmentedControlView(Context context) {
        super(context, null);
        //Initialize
        init(context);
        // Setup the view
        update();
    }

    public SegmentedControlView(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);

        //Initialize
        init(context);

        //Here's where overwrite the defaults with the values from the xml attributes
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SegmentedControlView,
                0, 0);

        try {

            mTextSize = attributes.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_textSize, 14);
            mStroke = attributes.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_stroke, 1);
            selectedColor = attributes.getColor(R.styleable.SegmentedControlView_scv_selectedColor, selectedColor);
            selectedTextColor = attributes.getColor(R.styleable.SegmentedControlView_scv_selectedTextColor, selectedTextColor);
            unselectedColor = attributes.getColor(R.styleable.SegmentedControlView_scv_unselectedColor, unselectedColor);
            unselectedTextColor = attributes.getColor(R.styleable.SegmentedControlView_scv_unselectedTextColor, selectedColor);
            mRadius = attributes.getDimensionPixelOffset(R.styleable.SegmentedControlView_scv_radius, 5);

            //Set text selectedColor state list
            textColorStateList = new ColorStateList(new int[][]{
                    {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[]{unselectedTextColor, selectedTextColor}
            );

            defaultSelection = attributes.getInt(R.styleable.SegmentedControlView_scv_defaultSelection, defaultSelection);
            equalWidth = attributes.getBoolean(R.styleable.SegmentedControlView_scv_equalWidth, equalWidth);
            stretch = attributes.getBoolean(R.styleable.SegmentedControlView_scv_stretch, stretch);
            identifier = attributes.getString(R.styleable.SegmentedControlView_scv_identifier);

            CharSequence[] itemArray = attributes.getTextArray(R.styleable.SegmentedControlView_scv_items);
            CharSequence[] valueArray = attributes.getTextArray(R.styleable.SegmentedControlView_scv_values);

            // TODO: Need to look into better setting up the item for the preview view
            if (this.isInEditMode()) {
                itemArray = new CharSequence[]{"选项1", "选项2", "选项3"};
            }

            //Item and value arrays need to be of the same length
            if (itemArray != null && valueArray != null) {
                if (itemArray.length != valueArray.length) {
                    throw new Exception("Item labels and value arrays must be the same size");
                }
            }

            if (itemArray != null) {

                if (valueArray != null) {
                    for (int i = 0; i < itemArray.length; i++) {
                        itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                    }

                } else {

                    for (CharSequence item : itemArray) {
                        itemMap.put(item.toString(), item.toString());
                    }

                }

            }

        } finally {
            attributes.recycle();
        }

        //Setup the view
        update();
    }

    private void init(Context context) {
        mCtx = context;
        //Needed for calling the right "setbackground" method
        mSdk = Build.VERSION.SDK_INT;
    }

    /**
     * Does the setup and re-setup of the view based on the currently set options
     */
    @TargetApi(16)
    private void update() {

        //Remove all views...
        this.removeAllViews();

        //Get size of two DP (size of stroke)
        /*int twoDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());*/

        //Ensure orientation is horizontal
        this.setOrientation(RadioGroup.HORIZONTAL);


        float textWidth = 0;
        options = new ArrayList<>();

        Iterator itemIterator = itemMap.entrySet().iterator();
        int i = 0;
        while (itemIterator.hasNext()) {

            Map.Entry<String, String> item = (Map.Entry) itemIterator.next();
            RadioButton radioButton = new RadioButton(mCtx);
            radioButton.setTextColor(textColorStateList);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (stretch) {
                params.weight = 1.0f;
            }
            if (i > 0) {
                params.setMargins(-mStroke, 0, 0, 0);
            }

            radioButton.setLayoutParams(params);


            //Clear out button drawable (text only)
            radioButton.setButtonDrawable(new StateListDrawable());

            //Create state list for background
            if (i == 0) {
                //Left
                GradientDrawable leftUnselected = generateDrawable(ITEM_LEFT, unselectedColor);
                GradientDrawable leftSelected = generateDrawable(ITEM_LEFT, selectedColor);
                StateListDrawable leftStateListDrawable = new StateListDrawable();
                leftStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, leftUnselected);
                leftStateListDrawable.addState(new int[]{android.R.attr.state_checked}, leftSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    radioButton.setBackgroundDrawable(leftStateListDrawable);
                } else {
                    radioButton.setBackground(leftStateListDrawable);
                }

            } else if (i == (itemMap.size() - 1)) {
                //Right
                GradientDrawable rightUnselected = generateDrawable(ITEM_RIGHT, unselectedColor);
                GradientDrawable rightSelected = generateDrawable(ITEM_RIGHT, selectedColor);
                StateListDrawable rightStateListDrawable = new StateListDrawable();
                rightStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, rightUnselected);
                rightStateListDrawable.addState(new int[]{android.R.attr.state_checked}, rightSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    radioButton.setBackgroundDrawable(rightStateListDrawable);
                } else {
                    radioButton.setBackground(rightStateListDrawable);
                }

            } else {
                //Middle
                GradientDrawable middleUnselected = generateDrawable(ITEM_MIDDLE, unselectedColor);
                GradientDrawable middleSelected = generateDrawable(ITEM_MIDDLE, selectedColor);
                StateListDrawable middleStateListDrawable = new StateListDrawable();
                middleStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, middleUnselected);
                middleStateListDrawable.addState(new int[]{android.R.attr.state_checked}, middleSelected);
                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                    radioButton.setBackgroundDrawable(middleStateListDrawable);
                } else {
                    radioButton.setBackground(middleStateListDrawable);
                }
            }

            radioButton.setLayoutParams(params);
            radioButton.setMinWidth(mStroke * 10);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            radioButton.setText(item.getKey());
            //这行代码主要是为兼容一些手机rom中的导致RadioButton文字不居中显示
            radioButton.setPadding(0, 0, 0, 0);
            textWidth = Math.max(radioButton.getPaint().measureText(item.getKey()), textWidth);
            options.add(radioButton);
            i++;
        }

        //We do this to make all the segments the same width
        for (RadioButton option : options) {
            if (equalWidth) {
                option.setWidth((int) (textWidth + (mStroke * 20)));
            }
            this.addView(option);
        }

        this.setOnCheckedChangeListener(selectionChangedlistener);

        if (defaultSelection > -1) {
            this.check((getChildAt(defaultSelection)).getId());
        }
    }

    /**
     * Get currently selected segment and the view identifier
     *
     * @return string array of identifier [0] value of currently selected segment [1]
     */
    public String[] getCheckedWithIdentifier() {
        return new String[]{identifier, itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString())};
    }

    /**
     * Get currently selected segment
     *
     * @return value of currently selected segment
     */
    public String getChecked() {
        return itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString());
    }

    /**
     * Used to pass along the selection change event
     * Calls onSelectionChangedListener with identifier and value of selected segment
     */
    private OnCheckedChangeListener selectionChangedlistener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mListener != null) {
                mListener.newSelection(identifier, itemMap.get(((RadioButton) group.findViewById(checkedId)).getText().toString()));
            }

        }
    };

    /**
     * Interface for for the selection change event
     */
    public interface OnSelectionChangedListener {
        public void newSelection(String identifier, String value);
    }

    /**
     * Sets the items and vaules for each segements.
     *
     * @param itemArray
     * @param valueArray
     * @throws Exception
     */
    public void setItems(String[] itemArray, String[] valueArray) throws Exception {

        itemMap.clear();

        if (itemArray != null && valueArray != null) {
            if (itemArray.length != valueArray.length) {
                throw new Exception("Item labels and value arrays must be the same size");
            }
        }

        if (itemArray != null) {

            if (valueArray != null) {
                for (int i = 0; i < itemArray.length; i++) {
                    itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                }

            } else {

                for (CharSequence item : itemArray) {
                    itemMap.put(item.toString(), item.toString());
                }

            }


        }

        update();
    }

    /**
     * Sets the items and vaules for each segements. Also provides a helper to setting the
     * default selection
     *
     * @param items
     * @param values
     * @param defaultSelection
     * @throws Exception
     */
    public void setItems(String[] items, String[] values, int defaultSelection) throws Exception {

        if (defaultSelection > (items.length - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            setItems(items, values);
        }
    }

    /**
     * Sets the item that is selected by default. Must be greater than -1.
     *
     * @param defaultSelection
     * @throws Exception
     */
    public void setDefaultSelection(int defaultSelection) throws Exception {
        if (defaultSelection > (itemMap.size() - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            update();
        }
    }

    /**
     * Sets the colors used when drawing the view. The primary color will be used for selected color
     * and unselected text color, while the secondary color will be used for unselected color
     * and selected text color.
     *
     * @param primaryColor
     * @param secondaryColor
     */
    public void setColors(int primaryColor, int secondaryColor) {
        this.selectedColor = primaryColor;
        this.selectedTextColor = secondaryColor;
        this.unselectedColor = secondaryColor;
        this.unselectedTextColor = primaryColor;

        //Set text selectedColor state list
        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
        );

        update();
    }

    /**
     * Sets the colors used when drawing the view
     *
     * @param selectedColor
     * @param selectedTextColor
     * @param unselectedColor
     * @param unselectedTextColor
     */
    public void setColors(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor) {
        this.selectedColor = selectedColor;
        this.selectedTextColor = selectedTextColor;
        this.unselectedColor = unselectedColor;
        this.unselectedTextColor = unselectedTextColor;

        //Set text selectedColor state list
        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
        );

        update();
    }

    /**
     * Used to set the selected value based on the value (not the visible text) provided in the
     * value array provided via xml or code
     *
     * @param value
     */
    public void setByValue(String value) {
        String buttonText = "";
        if (this.itemMap.containsValue(value)) {
            for (String entry : itemMap.keySet()) {
                if (itemMap.get(entry).equalsIgnoreCase(value)) {
                    buttonText = entry;
                }
            }
        }

        for (RadioButton option : options) {
            if (option.getText().toString().equalsIgnoreCase(buttonText)) {
                this.check(option.getId());
            }
        }

    }

    /**
     * Sets the mListener that gets called when a selection is changed
     *
     * @param listener
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mListener = listener;
    }

    /**
     * For use with multiple views. This identifier will be provided in the
     * onSelectionChanged mListener
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Set to true if you want each segment to be equal width
     *
     * @param equalWidth
     */
    public void setEqualWidth(boolean equalWidth) {
        this.equalWidth = equalWidth;
        update();
    }

    /**
     * Set to true if the view should be stretched to fill it's parent view
     *
     * @param stretch
     */
    public void setStretch(boolean stretch) {
        this.stretch = stretch;
        update();
    }

    /**
     * 创建GradientDrawable提供给每个RadioButton使用
     *
     * @param type
     * @param color
     * @return
     */
    private GradientDrawable generateDrawable(int type, int color) {
        float[] radius;
        if (type == ITEM_LEFT) {
            //第一个
            radius = new float[]{
                    mRadius, mRadius,
                    0, 0,
                    0, 0,
                    mRadius, mRadius
            };
        } else if (type == ITEM_RIGHT) {
            //最后一个
            radius = new float[]{
                    0, 0,
                    mRadius, mRadius,
                    mRadius, mRadius,
                    0, 0
            };
        } else {
            //中间的
            radius = new float[]{
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0
            };
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadii(radius);
        shape.setColor(color);
        shape.setStroke(mStroke, selectedColor);
        return shape;
    }

}
