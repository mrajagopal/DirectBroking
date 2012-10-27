package directbroking.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener
{
    private ProgressDialog dialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("Runnng onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        ImageButton loginButton = (ImageButton) findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(this);
        String colorStr = getResources().getString(R.color.grey);
        int color = Color.parseColor(getResources().getString(R.color.grey));
        System.out.printf("colorStr: %s colorInt: %d\n", colorStr, color);

        EditText passwordTextBox = (EditText) findViewById(R.id.Password);
        passwordTextBox.setOnEditorActionListener(new OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId==EditorInfo.IME_ACTION_DONE)
                {
                    onClick(v);
                }
            return false;
            }
        });
        isConnectedToNetwork();
        ImageView img = (ImageView) findViewById(R.id.NzxLogo);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nzx.com"));
            startActivity(intent);
        }
    });

    }


    @Override
    public void onStart()
    {
        super.onStart(); // Always call the superclass
        System.out.println("Runnng onStart");
    }

    @Override
    public void onPause()
    {
        super.onPause(); // Always call the superclass
        System.out.println("Runnng onPause");
    }

    @Override
    public void onResume()
    {
        super.onResume(); // Always call the superclass
        System.out.println("Runnng onResume");
        EditText accountNum = (EditText) findViewById(R.id.AccountNo);
        EditText password = (EditText) findViewById(R.id.Password);
        accountNum.setText("");
        password.setText("");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy(); // Always call the superclass
    }

    private boolean isConnectedToNetwork()
    {
        boolean haveNetworkConnection = true;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null)
        {
            Context context = getApplicationContext();
            CharSequence text = "Service requires network connection. Please check your connection.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            haveNetworkConnection = false;
        }
        return haveNetworkConnection;
    }
    // Implement the OnClickListener callback
    public void onClick(View v)
    {
        if (!isConnectedToNetwork())
        {
            return;
        }

        EditText accountNum = (EditText) findViewById(R.id.AccountNo);
        String accountNumString = accountNum.getText().toString();
        EditText password = (EditText) findViewById(R.id.Password);
        String passwordString = password.getText().toString();

        if ((accountNumString.equals("")) || (passwordString.equals("")))
        {
            displayNotification("Please login using your account number or username and password");
        }
        else
        {
            dialog = new ProgressDialog(this);
            dialog.setTitle("Please wait");
            dialog.setMessage("Verifying username and password...");
            dialog.show();

            LoginActionTask loginActionTask = new LoginActionTask(this);
            loginActionTask.execute(new String[] {accountNumString, passwordString});
        }
    }

    private void displayNotification(CharSequence text)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private class LoginActionTask extends AsyncTask<String,Void, String>
    {
        Context AppContext;

        private LoginActionTask(Context context)
        {
            AppContext = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String...inputs)
        {
            String accountNumString = inputs[0];
            String passwordString = inputs[1];
            String responseBody = "";

            HttpClient client = dbHttpClientInstance();

            try
            {
                HttpPost httppost = new HttpPost("https://www.directbroking.co.nz/DirectTrade/dynamic/signon.aspx?Login=Go+%3E%3E");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("username", accountNumString));
                nameValuePairs.add(new BasicNameValuePair("password", passwordString));
                nameValuePairs.add(new BasicNameValuePair("startin", "../secure/portfolios.aspx"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(httppost);
                responseBody = EntityUtils.toString(response.getEntity());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return responseBody;
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected void onPostExecute(String result)
        {
            dialog.dismiss();
            if(onLoginSuccess(result))
            {
//                Intent myPortfolio = new Intent(AppContext, DirectBrokingWebView.class);
                Intent myPortfolio = new Intent(AppContext, DBTextView.class);
                myPortfolio.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myPortfolio.putExtra("htmlString", fetchPortolioData(result));
                AppContext.startActivity(myPortfolio);
            }
        }

        private String fetchPortolioData(String htmlData)
        {
            Document document = Jsoup.parse(htmlData);
            String table = document.select("table[id=PortfolioPositionsTable]").outerHtml();

            return table;
        }

        private boolean onLoginSuccess(String htmlString)
        {
            boolean result= true;
            Document document = Jsoup.parse(htmlString);
            Element title = document.select("TITLE").first();

            if( title.text().equals("Login | Direct Broking"))
            {
                Elements elements = document.select("span[id~=SystemMsg1_lblText]");
                for (Element element : elements)
                {
                    displayNotification(element.html());
                    break;
                }

                result = false;
            }

            return result;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.loginmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.aboutus:
                String aboutUsString = "AboutUs";
                Intent aboutUsIntent = new Intent(this, DirectBrokingWebView.class);
                aboutUsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                aboutUsIntent.putExtra("userRequestItem", aboutUsString);
                this.startActivity(aboutUsIntent);
                break;

            case R.id.contactus:
                String contactUsString = "ContactUs";
                Intent contactUsIntent = new Intent(this, DirectBrokingWebView.class);
                contactUsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contactUsIntent.putExtra("userRequestItem", contactUsString);
                this.startActivity(contactUsIntent);
                break;

        }
        return true;
    }
    
    public static HttpClient client;
	public static HttpClient dbHttpClientInstance() 
	{
		if (client == null)
		{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		HttpParams params = new BasicHttpParams();
		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(params, schemeRegistry);
		client = new DefaultHttpClient(mgr, params);
		}
		return client;
	}
}
