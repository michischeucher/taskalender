package brothers.scheucher.taskalender;

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

public class LabelFragment extends Fragment {
    private static final String tag = "LabelFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_LABEL_ID = "label_id";

    private int label_id = -1;
    private static Context context;
    private static View view;
    private static LayoutInflater inflater;

    private Button new_button;
    private RelativeLayout ll;

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
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        ll = (RelativeLayout) inflater.inflate(R.layout.activity_label_view, container, false);

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

        if (child_labels.size() != 0 && label_id != -1) {
            View row = inflater.inflate(R.layout.text_description, null, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Untergeordnete Labels: ");
            label_view.addView(row);
        }

        for (final Label label : child_labels) {
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
        if (task_array.size() != 0) {
            View row = inflater.inflate(R.layout.text_description, null, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Aufgaben mit diesem Label: ");
            task_list.addView(row);
        }

        for (final Task task : task_array) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.task_in_list, task_list, false);

            TextView name_view = (TextView) rowView.findViewById(R.id.task_name);
            name_view.setText(task.getName());
            TextView duration_view = ((TextView)rowView.findViewById(R.id.task_duration));
            duration_view.setText(Util.getFormattedDuration(task.getRemaining_duration()));
            TextView notice_view = (TextView)rowView.findViewById(R.id.task_notice);
            if (!task.getNotice().equals("")) {
                notice_view.setVisibility(TextView.VISIBLE);
                notice_view.setText(task.getNotice());
            }

            Util.setColorOfDrawable(rowView, 0xA0000000 | task.getColor());
            if (Util.isDarkColor(task.getColor())) {
                name_view.setTextColor(0xFFFFFFFF);
                duration_view.setTextColor(0xFFFFFFFF);
                notice_view.setTextColor(0xFFFFFFFF);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
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
