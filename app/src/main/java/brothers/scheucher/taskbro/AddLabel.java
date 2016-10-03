package brothers.scheucher.taskbro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class AddLabel extends AppCompatActivity {
    private static final String tag = "AddLabel";
    private Label label;

    private Context context;
    private TextView color_view;
    private int selected_item = -1;
    private TextView parent_label;
    private TextView parent_label_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_label);

        context = this;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            int id = b.getInt("id");
            int parent_label_id = b.getInt("parent_label_id");
            if (id == -1) {
                label = new Label();
                label.setParent(parent_label_id);
                findViewById(R.id.add_label_title).requestFocus();
            } else {
                label = TaskBroContainer.getLabel(id);
            }
        } else {
            label = new Label();
            findViewById(R.id.add_label_title).requestFocus();

        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        color_view = ((TextView) findViewById(R.id.add_label_color));
        parent_label = (TextView) findViewById(R.id.add_label_parentlabel);
        parent_label_color = (TextView) findViewById(R.id.add_label_parentlabel_color);
        LinearLayout parent_label_container = (LinearLayout) findViewById(R.id.add_label_parentlabel_container);
        Button delete_button = (Button) findViewById(R.id.add_label_delete_button);

        if (label.getColor() != -1) {
            color_view.setBackgroundColor(0xFF000000 | label.getColor());
            color_view.setText("");
        } else {
            color_view.setBackgroundColor(0x00FFFFFF);
            color_view.setText(R.string.no_label);
        }
        ((TextView) findViewById(R.id.add_label_title)).setText(label.getName());

        if (label.getParent() != null) {
            parent_label.setText(label.getParent().getName());
            parent_label_color.setBackgroundColor(0xFF000000 | label.getParent().getColor());
        }

        parent_label_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                Label me = TaskBroContainer.getLabel(label.getId());
                final ArrayList<Label> label_sequence = TaskBroContainer.getLabelSequence(me);
                label_sequence.add(new Label(-1, "kein Label", 0x00FFFFFF));
                ArrayList<String> label_sequence_strings = new ArrayList<>();
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
                                    if (parent_label.getId() == -1) {
                                        no_label = true;
                                    } else {
                                        label.setParent(parent_label);
                                        int color = 0xFF000000 | parent_label.getColor();
                                        AddLabel.this.parent_label.setText(parent_label.getName());
                                        parent_label_color.setBackgroundColor(color);
                                    }
                                }
                                if (no_label) {
                                    parent_label.setText(R.string.no_label);
                                    parent_label_color.setBackgroundColor(0x00FFFFFF);
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


        color_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int r, g, b;
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
                        color_view.setBackgroundColor(0xFF000000 | color);
                        color_view.setText("");

                        //Log.d(tag, "r " + cp.getRed() + " g " + cp.getGreen() + " b " + cp.getBlue() + " => " + color);

                        cp.dismiss();
                    }
                });
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.delete(TaskBroContainer.getContext());
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
            String label_name = ((TextView) add_label_view.findViewById(R.id.add_label_title)).getText().toString();
            if (label_name.equals("")) {
                Toast toast = Toast.makeText(this, "Bitte geben Sie einen Namen ein!", Toast.LENGTH_LONG);
                toast.show();
                return true;
            }
            if (!label.checkIfLabelNameExists(label_name)) {
                Toast toast = Toast.makeText(this, "Dieser Name existiert bereits, bitte geben Sie einen anderen Namen ein!", Toast.LENGTH_LONG);
                toast.show();
                return true;

            }
            label.setName(label_name);
            label.save(this);
            Log.d(tag, "vor adden");
            TaskBroContainer.addLabelToList(label);
            Log.d(tag, "nach adden");
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
