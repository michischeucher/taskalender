package brothers.scheucher.taskbro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class LabelFragment extends Fragment {
    private static final String tag = "LabelFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_LABEL_ID = "label_id_global";
    private static boolean show_done_tasks = false;

    private int label_id_global = -1;
    private static Context context;
    private static View view;
    private static LayoutInflater inflater;

    private RelativeLayout ll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    // TODO: Rename and change types of parameters
    public static LabelFragment newInstance(int label_id) {
        LabelFragment fragment = new LabelFragment();
        Bundle args = new Bundle();
        Log.d(tag, "label_id = " + label_id);
        args.putInt(ARG_LABEL_ID, label_id);
        fragment.setArguments(args);
        return fragment;
    }

    public LabelFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_label_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_show_toggle_done_tasks) {
            Log.d(tag, "action_show_toggle_done_tasks");
            if (LabelFragment.show_done_tasks) {
                LabelFragment.show_done_tasks = false;
                item.setTitle("Erledigte Aufgaben anzeigen");
            } else {
                LabelFragment.show_done_tasks = true;
                item.setTitle("Erledigte Aufgaben ausblenden");
            }

            updateDoneTaskList();

            return true;
        }

        return super.onOptionsItemSelected(item);
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
            label_id_global = getArguments().getInt(ARG_LABEL_ID);
            Log.d(tag, "onCreateView: ID = " + label_id_global);
        }

        ll = (RelativeLayout) inflater.inflate(R.layout.activity_label_view, container, false);

        Label label = TaskBroContainer.getLabel(label_id_global);
        int label_id;
        if (label != null) {
            ((TextView) ll.findViewById(R.id.label_hirarchy)).setText(label.getHirarchyString());
            label_id = label.getId();
        } else {
            label_id = -1;
        }

        Util.setNewButtonListeners(ll, getActivity(), null, label_id);

        return ll;
    }

    private void updateLabelList() {
        Log.d(tag, "updateLabelList");
        LinearLayout label_view = (LinearLayout)view.findViewById(R.id.labels_list_fragment_labels);
        label_view.removeAllViews();
        if (context == null || TaskBroContainer.getLabels() == null) {
            Log.d(tag, "updateLabelList - context == null || TaskBroContainer.getLabels() == null");
            return;
        }
        ArrayList<Label> child_labels;
        if (label_id_global == -1) {
            child_labels = TaskBroContainer.getRootLabels();
        } else {
            child_labels = TaskBroContainer.getLabel(label_id_global).getChildLabels();
        }

        if (child_labels.size() != 0 && label_id_global != -1) {
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
        if (context == null || TaskBroContainer.getTasks() == null) {
            Log.d(tag, "updateTaskList - context == null || TaskBroContainer.getTasks() == null");
            return;
        }

        ArrayList<Task> task_array = TaskBroContainer.getTasksNotDone(label_id_global);
        if (label_id_global == -1) {
            View row = inflater.inflate(R.layout.text_description, null, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Aufgaben ohne Label");
            task_list.addView(row);
        } else {
            View row = inflater.inflate(R.layout.text_description, null, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Aufgaben mit diesem Label");
            task_list.addView(row);
        }

        if (task_array.size() == 0) {
            View rowView = inflater.inflate(R.layout.text_item, null, false);
            ((TextView)rowView.findViewById(R.id.text_of_item)).setText("Keine nicht erledigten Aufgaben gefunden");
            task_list.addView(rowView);
        }

        for (int i = (task_array.size() - 1); i >= 0; i--) {
            final Task task = task_array.get(i);
            if (!task.isDone()) {
                View rowView = task.createTaskInListView(inflater, task_list);
                task_list.addView(rowView);
            }
        }
    }

    private void updateDoneTaskList() {
        Log.d(tag, "updateDoneTaskList");
        LinearLayout task_list = (LinearLayout)view.findViewById(R.id.done_tasks_with_that_label);
        task_list.removeAllViews();
        if (context == null || TaskBroContainer.getTasks() == null || !show_done_tasks) {
            Log.d(tag, "updateDoneTaskList - context == null || TaskBroContainer.getTasks() == null || !show_done_tasks");
            return;
        }

        ArrayList<Task> task_array = TaskBroContainer.getTasks(label_id_global);
        View row = inflater.inflate(R.layout.text_description, null, false);
        ((TextView)row.findViewById(R.id.description_of_item)).setText("Erledigte Aufgaben");
        task_list.addView(row);

        boolean no_done_task = true;
        for (int i = (task_array.size() - 1); i >= 0; i--) {
            final Task task = task_array.get(i);
            if (task.isDone()) {
                View rowView = task.createTaskInListView(inflater, task_list);
                task_list.addView(rowView);
                no_done_task = false;
            }
        }
        if (no_done_task) {
            View no_done_task_view = inflater.inflate(R.layout.text_item, null, false);
            ((TextView)no_done_task_view.findViewById(R.id.text_of_item)).setText("Keine erledigten Aufgaben gefunden.");
            task_list.addView(no_done_task_view);
        }
    }


    @Override
    public void onResume() {
        Log.d(tag, "onResume");
        updateLabelList();
        updateTaskList();
        updateDoneTaskList();
        super.onResume();
    }

}
