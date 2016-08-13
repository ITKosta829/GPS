package turntotech.org.gps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DeanC on 7/14/2016.
 */
public class DisplayAddress extends DialogFragment {

    TextView street, route, neighborhood, state, zip;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.display_address, null);
        street = (TextView) v.findViewById(R.id.street2);
        street.setText(MainActivity.address_components.get("street_number"));
        route = (TextView) v.findViewById(R.id.route2);
        route.setText(MainActivity.address_components.get("route"));
        neighborhood = (TextView) v.findViewById(R.id.neighborhood2);
        neighborhood.setText(MainActivity.address_components.get("neighborhood"));
        state = (TextView) v.findViewById(R.id.state2);
        state.setText(MainActivity.address_components.get("administrative_area_level_1"));
        zip = (TextView) v.findViewById(R.id.zip2);
        zip.setText(MainActivity.address_components.get("postal_code"));

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );


        return b.create();
    }
}
