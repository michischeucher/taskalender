package brothers.scheucher.taskalender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class LabelFragment extends Fragment {
    private static final String tag = "LabelFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_LABEL_ID = "label_id";

    private int label_id = -1;
    private static Context context;
    private static View view;

    private Button new_button;


    private RelativeLayout ll;
    private Activity fa;

    // TODO: Rename and change types of parameters
    public static LabelFragment newInstance(int label_id) {
        LabelFragment fragment = new LabelFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LABEL_ID, label_id);
        fragment.setArguments(args);
        return fragment;
    }

    public LabelFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(tag, "onActivityCreated");

        context = getActivity();
        view = getView();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreateView");

        if (getArguments() != null) {
            label_id = getArguments().getInt(ARG_LABEL_ID);
            Log.d(tag, "onCreateView: ID = " + label_id);
        }

        fa = super.getActivity();
        ll = (RelativeLayout) inflater.inflate(R.layout.fragment_labels, container, false);

        Label label = TimeRank.getLabel(label_id);
        if (label != null) {
            ((TextView) ll.findViewById(R.id.label_hirarchy)).setText(label.getHirarchyString());
        }

        new_button = ((Button) ll.findViewById(R.id.new_button));
        new_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new label");
                Intent intent = new Intent(getActivity(), AddLabel.class);
                startActivity(intent);
            }
        });

        return ll;
    }

    private void updateLabelList() {
        Log.d(tag, "updateLabelList");
        LinearLayout label_view = (LinearLayout)view.findViewById(R.id.labels_list_fragment_labels);
        label_view.removeAllViews();
        if (context == null || TimeRank.getLabels() == null) {
            Log.d(tag, "updateLabelList - context == null || TimeRank.getLabels() == null");
            return;
        }
        ArrayList<Label> child_labels;
        if (label_id == -1) {
            child_labels = TimeRank.getRootLabels();
        } else {
            child_labels = TimeRank.getLabel(label_id).getChildLabels();
        }
        if (child_labels.size() == 0) {
            ((TextView)ll.findViewById(R.id.empty_label_list)).setVisibility(View.VISIBLE);
            return;
        } else {
            ((TextView)ll.findViewById(R.id.empty_label_list)).setVisibility(View.GONE);
        }

        for (final Label label : child_labels) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.label, label_view, false);

            TextView name_view = (TextView) rowView.findViewById(R.id.label_name);
            View color_view = (View) rowView.findViewById(R.id.label_color);
            RelativeLayout label_container = (RelativeLayout)rowView.findViewById(R.id.label_container);

            String name = label.getName();
            name_view.setText(name);
            int color = 0xFF000000 | label.getColor();
            color_view.setBackgroundColor(color);
            Log.d(tag, "set name to label_view name = " + name);

            label_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LabelFragment label_fragment = LabelFragment.newInstance(label.getId());
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, label_fragment).addToBackStack("hirarchy_in_labels").commit();
                }
            });

            color_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddLabel.class);
                    Bundle b = new Bundle();
                    b.putInt("id", label.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });


            label_view.addView(rowView);
        }
    }

    private void updateTaskList() {
        Log.d(tag, "updateLabelList");
        LinearLayout task_list = (LinearLayout)view.findViewById(R.id.tasks_with_that_label);
        task_list.removeAllViews();
        if (context == null || TimeRank.getTasks() == null) {
            Log.d(tag, "updateTaskList - context == null || TimeRank.getTasks() == null");
            return;
        }

        ArrayList<Task> task_array = TimeRank.getTasks(label_id);
        if (task_array.size() == 0) {
            ((TextView)ll.findViewById(R.id.empty_task_list)).setVisibility(View.VISIBLE);
            return;
        } else {
            ((TextView)ll.findViewById(R.id.empty_task_list)).setVisibility(View.GONE);
        }

        for (final Task task : task_array) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.task_in_list, task_list, false);

            TextView name_view = (TextView) rowView.findViewById(R.id.task_name);
            View task_line = (View) rowView.findViewById(R.id.task_line);

            name_view.setText(task.getName());
            float remaining_time = Util.calculateRemainingHours(new GregorianCalendar(), task.getDeadline());
            int color_alpha = Util.getColorCode(remaining_time, task.getColor());

            int color = 0xFF000000 | task.getColor();
            task_line.setBackgroundColor(color);

            name_view.setBackgroundColor(color_alpha);

            name_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewTask.class);
                    Bundle b = new Bundle();
                    b.putInt("id", task.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            task_list.addView(rowView);
        }
    }


    @Override
    public void onResume() {
        Log.d(tag, "onResume");
        updateLabelList();
        updateTaskList();
        super.onResume();
    }

}
