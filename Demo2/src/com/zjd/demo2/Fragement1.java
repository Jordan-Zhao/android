package com.zjd.demo2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class Fragement1 extends Fragment{
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        return inflater.inflate(R.layout.fragement1, container, false);  
    } 
	
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
        Button button = (Button) getActivity().findViewById(R.id.frg1_btn);  
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView textView = (TextView) getActivity().findViewById(R.id.frg2_txt);  
                Toast.makeText(getActivity(), textView.getText(), Toast.LENGTH_LONG).show();  
			}
		});
    }  
	
}
