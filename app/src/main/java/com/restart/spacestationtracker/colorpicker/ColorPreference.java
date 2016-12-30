/*
 * Copyright (C) 2015 Martin Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.restart.spacestationtracker.colorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.restart.spacestationtracker.R;

public class ColorPreference extends DialogPreference {
    private final String selectNoneButtonText;
    private Integer defaultColor;
    private final String noneSelectedSummaryText;
    private final CharSequence summaryText;
    private final boolean showAlpha;
    private final boolean showHex;
    private View thumbnail;

    public ColorPreference(Context context) {
        this(context, null);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        summaryText = super.getSummary();

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorPicker, 0, 0);
            selectNoneButtonText = a.getString(R.styleable.ColorPicker_colorpicker_selectNoneButtonText);
            noneSelectedSummaryText = a.getString(R.styleable.ColorPicker_colorpicker_noneSelectedSummaryText);
            showAlpha = a.getBoolean(R.styleable.ColorPicker_colorpicker_showAlpha, true);
            showHex = a.getBoolean(R.styleable.ColorPicker_colorpicker_showHex, true);
        } else {
            selectNoneButtonText = null;
            noneSelectedSummaryText = null;
            showAlpha = true;
            showHex = true;
        }
    }

    @Override
    protected void onBindView(View view) {
        thumbnail = addThumbnail(view);
        showColor(getPersistedIntDefaultOrNull());
        // Only call after showColor sets any summary text:
        super.onBindView(view);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        if (a.peekValue(index) != null && a.peekValue(index).type == TypedValue.TYPE_STRING) {
            defaultColor = Color.parseColor(standardiseColorDigits(a.getString(index)));
        } else {
            defaultColor = a.getColor(index, Color.GRAY);
        }
        return defaultColor;
    }

    private static String standardiseColorDigits(String s) {
        if (s.charAt(0) == '#' && s.length() <= "#argb".length()) {
            // Convert #[a]rgb to #[aa]rrggbb
            String ss = "#";
            for (int i = 1; i < s.length(); ++i) {
                ss += s.charAt(i);
                ss += s.charAt(i);
            }
            return ss;
        } else {
            return s;
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setColor(restorePersistedValue ? getColor() : defaultColor);
    }

    private View addThumbnail(View view) {
        LinearLayout widgetFrameView = ((LinearLayout) view.findViewById(android.R.id.widget_frame));
        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.removeAllViews();
        LayoutInflater.from(getContext()).inflate(
                isEnabled()
                        ? R.layout.color_preference_thumbnail
                        : R.layout.color_preference_thumbnail_disabled,
                widgetFrameView);
        return widgetFrameView.findViewById(R.id.thumbnail);
    }

    private Integer getPersistedIntDefaultOrNull() {
        return getSharedPreferences().contains(getKey())
                ? Integer.valueOf(getPersistedInt(Color.GRAY))
                : defaultColor;
    }

    private void showColor(Integer color) {
        Integer thumbColor = color == null ? defaultColor : color;
        if (thumbnail != null) {
            thumbnail.setVisibility(thumbColor == null ? View.GONE : View.VISIBLE);
            thumbnail.findViewById(R.id.colorPreview).setBackgroundColor(thumbColor == null ? 0 : thumbColor);
        }
        if (noneSelectedSummaryText != null) {
            setSummary(thumbColor == null ? noneSelectedSummaryText : summaryText);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final ColorPickerView picker = new ColorPickerView(getContext());

        picker.setColor(getPersistedInt(defaultColor == null ? Color.GRAY : defaultColor));
        picker.showAlpha(showAlpha);
        picker.showHex(showHex);
        builder
                .setTitle(null)
                .setView(picker)
                .setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int color = picker.getColor();
                        if (callChangeListener(color)) {
                            setColor(color);
                        }
                    }
                });
        if (selectNoneButtonText != null) {
            builder.setNeutralButton(selectNoneButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (callChangeListener(null)) {
                        setColor(null);
                    }
                }
            });
        }
    }

    private void removeSetting() {
        if (shouldPersist()) {
            getSharedPreferences().edit().remove(getKey()).apply();
        }
    }

    public void setColor(Integer color) {
        if (color == null) {
            removeSetting();
        } else {
            persistInt(color);
        }
        showColor(color);
    }

    public Integer getColor() {
        return getPersistedIntDefaultOrNull();
    }
}
