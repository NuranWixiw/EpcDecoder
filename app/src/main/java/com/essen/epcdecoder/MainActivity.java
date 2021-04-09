package com.essen.epcdecoder;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.essen.epcdecoder.Util.rfid.OnRfidReadListener;
import com.essen.epcdecoder.Util.rfid.RfidReaderListener;
import com.essen.epcdecoder.models.Brand;
import com.essen.epcdecoder.models.EAS_Alarm;
import com.essen.epcdecoder.models.ProductType;
import com.essen.epcdecoder.models.Color;
import com.essen.epcdecoder.models.Size;
import com.essen.epcdecoder.models.Quality;
import com.essen.epcdecoder.models.Model;
import com.essen.epcdecoder.models.Section;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends RfidBaseActivity {

    Button btn_connect, btn_get_detail;
    TextView Brand, Barkod, Section, ProductType, Model, Quality, Color, Size, EasAlarm;
    TextView txtBrandError, txtProducttypeError, txtSectionError, txtColorError, txtEASAlarmError, txtSizeError, txtQualityError, txtBarcodeError, txtModelError;
    LinearLayout ll_lcw_detail, ll_message;
    EditText et_tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        setDetailViewBinding();
        ll_lcw_detail = findViewById(R.id.ll_lcw_detail);
        ll_message = findViewById(R.id.ll_message);
        et_tag = findViewById(R.id.et_rfid_tag);
        btn_get_detail = findViewById(R.id.btn_get_detail);
        btn_get_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(et_tag.getText().toString().trim().length() == 0)
                    et_tag.setError("Alanı Doldurun");
               else {
                    GetTagDetail(et_tag.getText().toString());
                }

            }
        });
        btn_connect = findViewById(R.id.btn_tsl);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenRfidSelectDialog();
            }
        });
        setmRfidReaderListener(new RfidReaderListener() {
            @Override
            public void onReaderConnect() {
                btn_connect.setVisibility(View.GONE);
                btn_connect.setText("connected");
                Drawable d = getResources().getDrawable(R.drawable.bt_on);
                btn_connect.setBackgroundDrawable(d);


            }

            @Override
            public void onReaderDisconnect() {
                btn_connect.setVisibility(View.VISIBLE);
                btn_connect.setText("disconnected");
                Drawable d = getResources().getDrawable(R.drawable.bt_off);
                btn_connect.setBackgroundDrawable(d);

            }
        });
        setRfidReadListener(new OnRfidReadListener() {
            @Override
            public void OnRead(String tag) {

                GetTagDetail(tag);
            }
        });

    }

    private void setDetailViewBinding() {
        Brand = findViewById(R.id.txt_Brand);
        txtBrandError = findViewById(R.id.txt_brand_error);
        txtBrandError.setVisibility(View.GONE);
        Barkod = findViewById(R.id.txt_Barkod);
        txtBarcodeError = findViewById(R.id.txt_barcode_error);
        txtBarcodeError.setVisibility(View.GONE);
        Section = findViewById(R.id.txt_Section);
        txtSectionError = findViewById(R.id.txt_section_error);
        txtSectionError.setVisibility(View.GONE);
        ProductType = findViewById(R.id.txt_ProductType);
        txtProducttypeError = findViewById(R.id.txt_producttype_error);
        txtProducttypeError.setVisibility(View.GONE);
        Model = findViewById(R.id.txt_Model);
        txtModelError = findViewById(R.id.txt_model_error);
        txtModelError.setVisibility(View.GONE);
        Quality = findViewById(R.id.txt_Quality);
        txtQualityError = findViewById(R.id.txt_quality_error);
        txtQualityError.setVisibility(View.GONE);
        Color = findViewById(R.id.txt_Color);
        txtColorError = findViewById(R.id.txt_color_error);
        txtColorError.setVisibility(View.GONE);
        Size = findViewById(R.id.txt_Size);
        txtSizeError =findViewById(R.id.txt_size_error);
        txtSizeError.setVisibility(View.GONE);
        EasAlarm = findViewById(R.id.txt_EasAlarm);
        txtEASAlarmError = findViewById(R.id.txt_EASAlarm_error);
        txtEASAlarmError.setVisibility(View.GONE);


    }

    public void GetTagDetail(String tag) {
        // String hex = "0828816F87359840001E1C781BA08503";
        String hex = tag;

        try {
            String binaryString = Converter.hexToBin(hex);
            EpcModel epcModel = new EpcModel();
            epcModel.Version = Converter.binToHex(GetRange(binaryString, 0, 5));
            epcModel.Brand = Converter.binToHex(GetRange(binaryString, 5, 6));
            epcModel.Section = Converter.binToHex("00" + GetRange(binaryString, 11, 2));
            epcModel.ProductType = Converter.binToHex(GetRange(binaryString, 13, 4));
            GetMqcs(GetRangeFull(binaryString, 17, 40), epcModel);
            epcModel.IsActive = GetRange(binaryString, 57, 1);
            //000000 tane atlanacak GetCheckRecordingBinary - 64
            epcModel.SerialNumber = binaryToHex(GetRangeFull(binaryString, 64, 32));
            epcModel.CreationDate = binaryToHex(GetRangeFull(binaryString, 96, 11));
            //000001 tane atlanacak GetCheckRecordingBinary - 113
            //versiyon 5 tane atla 118
            // 5 tane provider için atla 123
            // 3 tane GetFreeBitsBinary için atla 126
            epcModel.TagEAS = GetRange(binaryString, 126, 1);
            epcModel.TagType = GetRange(binaryString, 127, 1);




            setEpcModelView(epcModel);
        } catch (Exception ex) {
            ll_lcw_detail.setVisibility(View.GONE);
            ll_message.setVisibility(View.VISIBLE);

          // Toast.makeText(this,"Hata" , Toast.LENGTH_LONG).show();
        }


    }


    private void setEpcModelView(EpcModel epcModel) {
        ll_lcw_detail.setVisibility(View.VISIBLE);
        ll_message.setVisibility(View.GONE);

        brands.add(new Brand("1", "Zara"));
        brands.add(new Brand("2", "PullAndBear"));
        brands.add(new Brand("4", "Bershka"));
        brands.add(new Brand("6", "Stradivarius"));
        brands.add(new Brand("7", "Oysho"));
        brands.add(new Brand("8", "Lefties"));
        brands.add(new Brand("11", "MassimoDutti"));
        brands.add(new Brand("14", "ZaraHome"));
        brands.add(new Brand("16", "ZaraSur"));
        brands.add(new Brand("18", "Uteerque"));
        brands.add(new Brand("63", "Test"));

        productTypes.add(new ProductType("0", "Clothing"));
        productTypes.add(new ProductType("1", "Footwear"));

        sections.add(new Section("1", "Kadın"));
        sections.add(new Section("2", "Erkek"));
        sections.add(new Section("3", "Çocuk"));

        eas_alarms.add(new EAS_Alarm("0","Wth EAS"));
        eas_alarms.add(new EAS_Alarm("1","Without EAS"));


        SetDisplayNames(epcModel);


    }

    List<com.essen.epcdecoder.models.Brand> brands = new ArrayList<>();
    List<com.essen.epcdecoder.models.Quality> qualities = new ArrayList<>();
    List<com.essen.epcdecoder.models.Size> sizes = new ArrayList<>();
    List<com.essen.epcdecoder.models.Model> models = new ArrayList<>();
    List<com.essen.epcdecoder.models.Color> colors = new ArrayList<>();
    List<com.essen.epcdecoder.models.Section> sections = new ArrayList<>();
    List<com.essen.epcdecoder.models.ProductType> productTypes = new ArrayList<>();
    List<com.essen.epcdecoder.models.EAS_Alarm> eas_alarms = new ArrayList<>();


    private void SetDisplayNames(EpcModel epcModel) {
        boolean isHaveBrand = false;
        for (Brand brand : brands) {
            if (brand.getId().equals(epcModel.Brand)) {
                Brand.setText(brand.getDisplayValue());
                isHaveBrand = true;
            }
        }
        if (!isHaveBrand) {
            Brand.setText(epcModel.Brand);
            txtBrandError.setVisibility(View.VISIBLE);
        }else{
            txtBrandError.setVisibility(View.GONE);
        }

        boolean isHaveSection = false;
        for (Section section : sections) {
            if (section.getId().equals(epcModel.Section)) {
                Section.setText(section.getDisplayValue());
                isHaveSection = true;
            }
        }
        if (!isHaveSection) {
            Section.setText(epcModel.Section);
            txtSectionError.setVisibility(View.VISIBLE);
        }else{
            txtSectionError.setVisibility(View.GONE);
        }


        boolean isHaveProductType = false;
        for (ProductType productType : productTypes) {
            if (productType.getId().equals(epcModel.ProductType)) {
                ProductType.setText(productType.getDisplayValue());
                isHaveProductType = true;
            }
        }
        if (!isHaveProductType) {
            ProductType.setText(epcModel.ProductType);
            txtProducttypeError.setVisibility(View.VISIBLE);
        }else{
            txtProducttypeError.setVisibility(View.GONE);
        }

        boolean isHaveEAS_Alarm = false;
        for (EAS_Alarm eas_alarm : eas_alarms) {
            if (eas_alarm.getId().equals(epcModel.TagEAS)) {
                EasAlarm.setText(eas_alarm.getDisplayValue());
                isHaveEAS_Alarm = true;
            }
        }
        if (!isHaveEAS_Alarm) {
            EasAlarm.setText(epcModel.TagEAS);
            txtEASAlarmError.setVisibility(View.VISIBLE);
        }else{
            txtEASAlarmError.setVisibility(View.GONE);
        }
        boolean isHaveColor = false;
        for (Color color : colors) {
            if (color.getId().equals(epcModel.Color)) {
                Color.setText(color.getId());
                isHaveColor = true;
            }
        }
        if (!isHaveColor) {
            Color.setText(epcModel.Color);
            txtColorError.setVisibility(View.GONE);
           // txtColorError.setVisibility(View.VISIBLE);
        }else{
            txtColorError.setVisibility(View.GONE);
        }

        boolean isHaveSize = false;
        for (Size size : sizes) {
            if (size.getId().equals(epcModel.Size)) {
                Size.setText(size.getId());
                isHaveSize = true;
            }
        }
        if (!isHaveSize) {
            Size.setText(epcModel.Size);
          //  txtSizeError.setVisibility(View.VISIBLE);
            txtSizeError.setVisibility(View.GONE);
        }else{
            txtSizeError.setVisibility(View.GONE);
        }
        boolean isHaveModel = false;
        for (Model model : models) {
            if (model.getId().equals(epcModel.Model)) {
                Model.setText(model.getId());
                isHaveModel = true;
            }
        }
        if (!isHaveModel) {
            Model.setText(epcModel.Model);
           // txtModelError.setVisibility(View.VISIBLE);
            txtModelError.setVisibility(View.GONE);
        }else{
            txtModelError.setVisibility(View.GONE);
        }

        boolean isHaveQuality = false;
        for (Quality quality : qualities) {
            if (quality.getId().equals(epcModel.Quality)) {
                Quality.setText(quality.getId());
                isHaveQuality = true;
            }
        }
        if (!isHaveQuality) {
            Quality.setText(epcModel.Quality);
          //  txtQualityError.setVisibility(View.VISIBLE);
            txtQualityError.setVisibility(View.GONE);
        }else{
            txtQualityError.setVisibility(View.GONE);
        }


       // Quality.setText(epcModel.Quality);

        String zeroOrOne="0";
        if (epcModel.Brand.equals("7") || epcModel.Brand.equals("8")){
            zeroOrOne = "1";
        }

        String code = zeroOrOne+epcModel.Model+epcModel.Quality+epcModel.Color+epcModel.Size;
        Barkod.setText(code+ String.valueOf(checkSum(code)));
    }


    public String GetRangeFull(String binary, int start, int length) {

        String value = binary.substring(start, start + length);
        return value;
    }

    public String GetRange(String binary, int start, int length) {

        String value = binary.substring(start, start + length);

        if (value.length() > 3) {
            return value.substring(value.length() - 4, value.length());
        }
        return value;
    }

    public void GetMqcs(String binary, EpcModel epcModel) {
        String value = binaryToHex(binary);

        epcModel.Size = value.substring(value.length() - 2, value.length());
        epcModel.Color = value.substring(value.length() - 5, value.length() - 2);
        epcModel.Quality = value.substring(value.length() - 8, value.length() - 5);
        epcModel.Model = value.substring(0, value.length() - 8);

    }

    public static String binaryToHex(String bin) {
        String hex = Long.toHexString(Long.parseLong(bin, 2));
        BigInteger bi = new BigInteger(hex, 16);
        return String.valueOf(bi.longValue());
    }


   // Barkod hesaplama Checkdigit
    public int checkSum(String code){
        int val=0;
        for(int i=0;i<code.length();i++){
            val+=((int)Integer.parseInt(code.charAt(i)+""))*((i%2==0)?1:3);
        }

        int checksum_digit = 10 - (val % 10);
        if (checksum_digit == 10) checksum_digit = 0;

        return checksum_digit;
    }


}