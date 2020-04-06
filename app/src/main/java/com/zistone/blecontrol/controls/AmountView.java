package com.zistone.blecontrol.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zistone.blecontrol.R;

import java.text.DecimalFormat;

public class AmountView extends LinearLayout implements View.OnClickListener, TextWatcher {

    private static final String TAG = "AmountView";

    private OnAmountChangeListener _lister;
    private TextView _text;
    private Button _btnAdd;
    private Button _btnCut;
    private double _step = 0.0, _current = 0.0, _min = 0.0, _max = 0.0;

    public interface OnAmountChangeListener {
        void onAmountChange(View view, double current);
    }

    public AmountView(Context context) {
        this(context, null);
    }

    public AmountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.amount_view, this);
        _text = findViewById(R.id.text);
        _text.addTextChangedListener(this);
        _btnAdd = findViewById(R.id.btnAdd);
        _btnAdd.setOnClickListener(this);
        _btnCut = findViewById(R.id.btnCut);
        _btnCut.setOnClickListener(this);
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.AmountView);
        int btnWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_btnWidth, LayoutParams.WRAP_CONTENT);
        int tvWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_tvWidth, 50);
        int tvTextSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_tvTextSize, 0);
        int btnTextSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_btnTextSize, 0);
        obtainStyledAttributes.recycle();
        LayoutParams btnParams = new LayoutParams(btnWidth, LayoutParams.MATCH_PARENT);
        _btnAdd.setLayoutParams(btnParams);
        _btnCut.setLayoutParams(btnParams);
        if (btnTextSize != 0) {
            _btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnTextSize);
            _btnCut.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnTextSize);
        }
        LayoutParams textParams = new LayoutParams(tvWidth, LayoutParams.MATCH_PARENT);
        _text.setLayoutParams(textParams);
        if (tvTextSize > 0) {
            _text.setTextSize(tvTextSize);
        }
    }

    @Override
    public void onClick(View v) {
        _current = Double.valueOf(_text.getText().toString());
        switch (v.getId()) {
            case R.id.btnAdd:
                if (_current < _max) {
                    _current += _step;
                }
                break;
            case R.id.btnCut:
                if (_current > _min && _current >= _step) {
                    _current -= _step;
                }
                break;
        }
        _text.setText(new DecimalFormat("0.0").format(_current));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (_lister != null) {
            _lister.onAmountChange(this, _current);
        }
    }

    public void setLister(OnAmountChangeListener lister) {
        this._lister = lister;
    }

    public void setMin(int min) {
        this._min = min;
    }

    public void setMax(int max) {
        this._max = max;
    }

    public void setStep(double step) {
        this._step = step;
    }

    public void setCurrent(double current) {
        this._current = current;
        _text.setText(new DecimalFormat("0.0").format(_current));
    }

    public double getCurrent() {
        return _current;
    }

}
