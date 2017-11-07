package website.amoeba.forestbewithme;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;


public class MapNameInputDialog extends DialogFragment {
    private String mapName;

    public String getMapName() {
        return mapName;
    }

    public interface InputDialogListener{
        public void onNegative(MapNameInputDialog dialog);
        public void onPositive(MapNameInputDialog dialog);
    }
    InputDialogListener mListener;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mListener=(InputDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inputDialogView =inflater.inflate(R.layout.map_name_input,null);
        final EditText editText = (EditText)inputDialogView.findViewById(R.id.editText);
        builder.setView(inputDialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapName=editText.getText().toString();
                        mListener.onPositive(MapNameInputDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNegative(MapNameInputDialog.this);
                    }
                });
        return builder.create();
    }
}

