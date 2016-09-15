package brothers.scheucher.taskbro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LabelAdapter extends ArrayAdapter<Label> {
    private static final String tag = "LabelAdapter";

    private final Context context;
    private ArrayList<Label> labels;
    private FragmentManager fragment_manager;

    public LabelAdapter(Context context, ArrayList<Label> labels, FragmentManager fragment_manager) {
        super(context, R.layout.label, labels);
        this.context = context;
        this.labels = labels;
        for (Label l : labels) {
            Log.d(tag, "label = " + l.getName());
        }
        this.fragment_manager = fragment_manager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.label, parent, false);

        Label label = labels.get(position);

        // 3. Get the views from the rowView
        TextView name_view = (TextView) rowView.findViewById(R.id.label_name);
        View color_view = (View) rowView.findViewById(R.id.label_color);

        String name = label.getName();
        name_view.setText(name);
        int color = 0xFF000000 | label.getColor();
        color_view.setBackgroundColor(color);
        Log.d(tag, "set name to label_view name = " + name);


        attachClickItemListener(name_view, color_view, position);
        attachLongClickItemListener(rowView, position);

        return rowView;
    }

    private void attachLongClickItemListener(View row_view, final int position) {
        row_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //View prompts_view = inflater.inflate(R.layout.long_click_artist, null);
                //builder.setView(prompts_view);
/*                builder.setItems(R.array.long_click_artist_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface duration_dialog, int which) {
                        Log.d("ArtistAdapter", "item selected = " + which);

                        switch (which) {
                            case 0:
                                startArtistActivity(position);
                                break;
                            case 1:
                                selectArtist(v, position);
                                break;
                            case 2:
                                Artist artist_to_change = artists.get(position);
                                Artist.changeArtistNameByAskingTheUser(context, artist_to_change);
                                break;
                            case 3:
                                Artist artist_to_add = artists.get(position);
                                int not_added_songs = MetaDataHelper.insertIntoCurrentSongs(artist_to_add.getSongs());
                                UsefulFunctions.showNotAddedSongsToast(not_added_songs);
                                break;
                            default:
                                Log.d("ArtistAdapter", "not a specified action");
                        }
                    }
                });

                builder.setTitle("Aktion auswählen");
                AlertDialog duration_dialog = builder.create();
                duration_dialog.show();
                */
                return true;
            }

        });
    }
    private void attachClickItemListener(View label_view, View color_view, final int position) {
        color_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "onClick position = " + position);
                ArrayList<Label> all_child_labels = labels.get(position).getChildLabels();
                for (Label l : all_child_labels) {
                    Log.d(tag, "child: " + l.description());
                }

                Intent intent = new Intent(context, AddLabel.class);
                Bundle b = new Bundle();
                Label l = labels.get(position);
                b.putInt("id", l.getId());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
    }

}
