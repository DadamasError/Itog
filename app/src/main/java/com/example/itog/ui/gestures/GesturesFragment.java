package com.example.itog.ui.gestures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.itog.R;
import com.example.itog.databinding.FragmentGesturesBinding;

public class GesturesFragment extends Fragment {

    private FragmentGesturesBinding binding;
    private CardView btnFist, btnLove, btnPalm, btnScissors, btnFirFing;
    private TextView activeLabelFist, activeLabelLove, activeLabelPalm,
            activeLabelScissors, activeLabelFirFing;
    private ImageView iconFist, iconLove, iconPalm, iconScissors, iconFirFing;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGesturesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnFist = root.findViewById(R.id.btnFist);
        btnLove = root.findViewById(R.id.btnLove);
        btnPalm = root.findViewById(R.id.btnPalm);
        btnScissors = root.findViewById(R.id.btnScissors);
        btnFirFing = root.findViewById(R.id.btnFirFing);

        activeLabelFist = root.findViewById(R.id.activeLabelFist);
        activeLabelLove = root.findViewById(R.id.activeLabelLove);
        activeLabelPalm = root.findViewById(R.id.activeLabelPalm);
        activeLabelScissors = root.findViewById(R.id.activeLabelScissors);
        activeLabelFirFing = root.findViewById(R.id.activeLabelFirFing);

        iconFist = root.findViewById(R.id.iconFist);
        iconLove = root.findViewById(R.id.iconLove);
        iconPalm = root.findViewById(R.id.iconPalm);
        iconScissors = root.findViewById(R.id.iconScissors);
        iconFirFing = root.findViewById(R.id.iconFirFing);

        btnFist.setOnClickListener(v -> selectButton(ButtonType.FIST));
        btnLove.setOnClickListener(v -> selectButton(ButtonType.LOVE));
        btnPalm.setOnClickListener(v -> selectButton(ButtonType.PALM));
        btnScissors.setOnClickListener(v -> selectButton(ButtonType.SCISSORS));
        btnFirFing.setOnClickListener(v -> selectButton(ButtonType.FIRFIN));

        return root;
    }

    private void selectButton(ButtonType type) {

        activeLabelFist.setVisibility(View.GONE);
        activeLabelLove.setVisibility(View.GONE);
        activeLabelPalm.setVisibility(View.GONE);
        activeLabelScissors.setVisibility(View.GONE);
        activeLabelFirFing.setVisibility(View.GONE);

        iconFist.setSelected(false);
        iconLove.setSelected(false);
        iconPalm.setSelected(false);
        iconScissors.setSelected(false);
        iconFirFing.setSelected(false);

        switch (type) {
            case FIST:
                activeLabelFist.setVisibility(View.VISIBLE);
                iconFist.setSelected(true);
                //sendProsthesisCommand(1);        //удалить
                break;
            case LOVE:
                activeLabelLove.setVisibility(View.VISIBLE);
                iconLove.setSelected(true);
                //sendProsthesisCommand(2);        //удалить
                break;
            case PALM:
                activeLabelPalm.setVisibility(View.VISIBLE);
                iconPalm.setSelected(true);
                //sendProsthesisCommand(3);        //удалить
                break;
            case SCISSORS:
                activeLabelScissors.setVisibility(View.VISIBLE);
                iconScissors.setSelected(true);
                //sendProsthesisCommand(4);        //удалить
                break;
            case FIRFIN:
                activeLabelFirFing.setVisibility(View.VISIBLE);
                iconFirFing.setSelected(true);
                //sendProsthesisCommand(5);        //удалить
                break;
        }
    }

    /*private void sendProsthesisCommand(int command) {      //удалить
        if (prosthesisController) {
            sendCommand(command);
        }
    }*/

    private enum ButtonType {
        FIST, LOVE, PALM, SCISSORS, FIRFIN
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}