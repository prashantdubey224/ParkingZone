package com.dubeyprashant.android.parkingzone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity {
    private boolean isLoggedOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);



        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get("first_name").toString();
        String surname = inBundle.get("last_name").toString();
        String imageUrl = inBundle.get("image_url").toString();
        //String address = inBundle.get("address").toString();


        TextView nameView = (TextView) findViewById(R.id.nameAndSurname);
        TextView addressView = (TextView) findViewById(R.id.address);
        TextView emailIdView = (TextView) findViewById(R.id.emailId);
        nameView.setText("" + name + " " + surname);
        if(inBundle.get("email_id")!=null){
            emailIdView.setText(""+ inBundle.get("email_id")+"");
        }else {
            emailIdView.setText("Not get email id");
        }

        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                LoginManager.getInstance().logOut();
                Intent login = new Intent(UserProfileActivity.this, LogInActivity.class);
                startActivity(login);
               isLoggedOut = true;
                finish();
            }
        });
        /*requesting to download the Image*/
        new UserProfileActivity.DownloadImage((ImageView)findViewById(R.id.profileImage)).execute(imageUrl);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        if(isLoggedOut){

            Toast.makeText(UserProfileActivity.this, "You logged Out", Toast.LENGTH_SHORT).show();
        }

    }


    /* use AsyncTask(new thread) to download the Image*/


    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImage(ImageView bmImage){
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls){
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try{
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            }catch (Exception e){
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result){
            bmImage.setImageBitmap(result);
        }

    }
}
