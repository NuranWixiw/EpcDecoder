package com.essen.epcdecoder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailActivity extends Activity {

   // ProductListModel productList;
   // Manager_Dialog manager_dialog;

   TextView Brand,Type,Section,ProductType,Model,Quality,Color,Size,Amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Brand = findViewById(R.id.txt_Brand);
        Type = findViewById(R.id.txt_Type);
        Section = findViewById(R.id.txt_Section);
        ProductType = findViewById(R.id.txt_ProductType);
        Model = findViewById(R.id.txt_Model);
        Quality = findViewById(R.id.txt_Quality);
        Color = findViewById(R.id.txt_Color);
        Size = findViewById(R.id.txt_Size);
        Amount = findViewById(R.id.txt_Amount);




    }


}