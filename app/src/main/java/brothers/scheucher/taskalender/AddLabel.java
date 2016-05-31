package brothers.scheucher.taskalender;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class AddLabel extends ActionBarActivity {
    private static final String tag = "AddLabel";
    private Label label;

    TextView add_label_color;
    LinearLayout add_label_parentlabel_container;
    private Button delete_button;
    private Context context;
    private int selected_item = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_label);

        context = this;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            int id = b.getInt("id");
            label = TimeRank.getLabel(id);
        } else {
            label = new Label();
        }

//        SQLiteStorageHelper.getInstance(this, 1).addAllTasksFromDatabase();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        add_label_color = ((TextView) findViewById(R.id.add_label_color));
        delete_button = ((Button) findViewById(R.id.add_label_delete_button));

        if (label.getColor() != -1) {
            add_label_color.setBackgroundColor(0xFF000000 | label.getColor());
            add_label_color.setText("");
        } else {
            add_label_color.setBackgroundColor(0x00FFFFFF);
            add_label_color.setText("Kein Label");
        }
        ((TextView)findViewById(R.id.add_label_title)).setText(label.getName());

        add_label_parentlabel_container = ((LinearLayout)findViewById(R.id.add_label_parentlabel_container));
        add_label_parentlabel_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final ArrayList<Label> label_sequence = TimeRank.getLabelSequence();
                label_sequence.add(new Label(-1, "kein Label", 0x00FFFFFF));
                ArrayList<String> label_sequence_strings = new ArrayList<String>();
                int i = 0;
                for (Label l : label_sequence) {
                    label_sequence_strings.add(l.getLabelStringWithHirarchie());
                    if (label.getParent() == l) {
                        selected_item = i;
                    }
                    i++;
                }
                builder.setSingleChoiceItems(label_sequence_strings.toArray(new CharSequence[label_sequence_strings.size()])
                        , selected_item, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected_item = which;
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                boolean no_label = false;
                                if (selected_item == -1) {
                                    no_label = true;
                                } else {
                                    Label parent_label = label_sequence.get(selected_item);
                                    if(parent_label.getId() == -1) {
                                        no_label = true;
                                    } else {
                                        label.setParent(parent_label);
                                        int color = 0xFF000000 | parent_label.getColor();
                                        ((TextView) findViewById(R.id.add_label_parentlabel)).setText(parent_label.getName());
                                        ((TextView) findViewById(R.id.add_label_parentlabel_color)).setBackgroundColor(color);
                                    }
                                }
                                if (no_label) {
                                    ((TextView)findViewById(R.id.add_label_parentlabel)).setText("kein Label");
                                    ((TextView)findViewById(R.id.add_label_parentlabel_color)).setBackgroundColor(0x00FFFFFF);
                                    label.setParent(null);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the duration_dialog
                                Log.d(tag, "cancelled");
                            }
                        });
                // Create the AlertDialog object and return it

                builder.show();
            }
        });


        add_label_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int r = 0;
                int g = 0;
                int b = 0;
                if (label.getColor() != -1) {
                    r = label.getColor() & 0xFF0000;
                    r = r >> 16;
                    g = label.getColor() & 0x00FF00;
                    g = g >> 8;
                    b = label.getColor() & 0x0000FF;
                } else {
                    r = Util.getRandomColorComponent();
                    g = Util.getRandomColorComponent();
                    b = Util.getRandomColorComponent();
                }
                final ColorPickerDialog cp = new ColorPickerDialog(AddLabel.this, r, g, b);
                cp.show();
                Button okColor = (Button) cp.findViewById(R.id.okColorButton);
                okColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /* You can get single channel (value 0-255) */
                        int color = 0;
                        color += cp.getRed() << 16;
                        color += cp.getGreen() << 8;
                        color += cp.getBlue();

                        label.setColor(color);
                        add_label_color.setBackgroundColor(0xFF000000 | color);
                        add_label_color.setText("");

                        Log.d(tag, "r " + cp.getRed() + " g " + cp.getGreen() + " b " + cp.getBlue() + " => " + color);

                        cp.dismiss();
                    }
                });
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.delete(TimeRank.getContext());
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            View add_label_view = findViewById(R.id.add_label);
            label.setName((String.valueOf(((TextView) add_label_view.findViewById(R.id.add_label_title)).getText())));
            label.save(this);
            TimeRank.addLabelToList(label);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
