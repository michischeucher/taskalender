package brothers.scheucher.taskalender;

import android.content.Context;

import java.util.ArrayList;

public class Label implements Comparable {
    private static final String tag = "Label";

    private int id;
    private String name;
    private int color;
    private Label parent;

    //database
    public static final String DB_TABLE = "LabelTable";
    public static final String DB_COL_ID = "LabelID";
    public static final String DB_COL_NAME = "LabelName";
    public static final String DB_COL_COLOR = "LabelColor";
    public static final String DB_COL_PARENT_ID = "LabelParent";

    public Label() {
        this.id = -1;
        this.name = "";
        this.color = -1;
        this.parent = null;
    }

    public Label(int id) {
        this.id = id;
        this.name = "";
        this.color = -1;
        this.parent = null;
    }

    public Label(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.parent = null;
    }
    public Label(String name, int color) {
        this.id = -1;
        this.name = name;
        this.color = color;
        this.parent = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        if (color == -1 && parent != null) {
            //give the color from parent (if exist)
            return parent.getColor();
        } else {
            return color;
        }
    }

    public void setColor(int color) {
        this.color = color;
    }
    public void removeColor() {
        this.color = -1;
    }

    public Label getParent() {
        return parent;
    }

    public void setParent(Label parent) {
        this.parent = parent;
    }

    public ArrayList<Label> getChildLabels() {
        ArrayList<Label> child_labels = new ArrayList<Label>();
        for (Label l : TimeRank.getLabels()) {
            if (this.equals(l.parent)) {
                child_labels.add(l);
            }
        }
        return child_labels;
    }

    public String description() {
        return "Label id = " + this.id + " name = " + this.name + " color = " + this.getColor();
    }

    public String getLabelStringWithHirarchie() {
        int hirarchie = 0;
        Label current = this.parent;
        while (current != null) {
            current = current.parent;
            hirarchie++;
        }
        String ret = "";
        while (hirarchie > 0) {
            ret += "-";
            hirarchie--;
            if (hirarchie == 0){
                ret += "> ";
            }
        }

        ret += this.getName();
        return ret;
    }

    @Override
    public int compareTo(Object another) {
        Label other = (Label)another;
        return this.getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object o) {
        Label other = (Label)o;
        if (other != null
                && this.name.equals(other.getName())
                && this.color == other.getColor()) {
            return true;
        } else {
            return false;
        }
    }

    public void save(Context context) {
        if (id == -1) {
            id = TimeRank.getNewLabelID();
        }
        SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
        db_helper.openDB();
        db_helper.saveLabel(this);
        db_helper.closeDB();
        db_helper.close();

    }

    public int getParentID() {
        if (parent == null) {
            return -1;
        } else {
            return parent.getId();
        }
    }

    public void setParent(int parent_id) {
        Label parent = TimeRank.getLabel(parent_id);
        this.parent = parent;
    }

    public void delete(Context context) {
        if (id == -1) {
            return;
        } else {
            SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
            db_helper.openDB();
            db_helper.deleteLabel(this);
            db_helper.closeDB();
            db_helper.close();
            TimeRank.deleteLabelFromList(this);

            for (Task t : TimeRank.getTasks()) {
                if (t.getLabelIds().contains(this.id)) {
                    t.getLabelIds().remove(Integer.valueOf(this.id));
                }
            }
            ArrayList<Label> labels_to_delete = new ArrayList<Label>();
            for (Label l : TimeRank.getLabels()) {
                if (l.getParent() == this) {
                    labels_to_delete.add(l);
                }
            }
            for (int i = 0; i < labels_to_delete.size(); i++) {
                labels_to_delete.get(i).delete(context);
            }

        }
    }

    public String getHirarchyString() {
        String ret = "";
        Label current = this;
        while (current != null) {
            ret = current.getName() + " > " + ret;
            current = current.parent;
        }
        return ret;
    }
}
