package phcar.aje;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button serveur_button = (Button) findViewById(R.id.serveur);
        serveur_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Créér un intent et lancer l'activité serveur
                Intent intent = new Intent(MainActivity.this, ServeurActivity.class);
                startActivity(intent);

            }
        });

        Button client_button = (Button) findViewById(R.id.client);
        client_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Créér un intent et lancer l'activité client
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);

            }
        });
    }
}
