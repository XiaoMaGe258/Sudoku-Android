package android.hfad.com.sudoku;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

public class LadderboardCell extends AppCompatTextView {

    public LadderboardCell(final Context context, final String text, int width) {
        super(context);
        setText(text);
        setTextColor(Color.WHITE);
        int padding = (int) AppConstant.convertDpToPixel(3, context);
        setPadding(0, padding, 0, padding);
        setGravity(Gravity.CENTER);
        setMaxLines(1);
        setWidth(width);
        setTypeface(AppConstant.APP_FONT);
        
        if(text.length() > 0) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, getText(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
