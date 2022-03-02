package contacts.interfaces;

import android.content.Context;

public abstract class WithLabel {
    private String mainData;
    private int contactId;
    private int labelId;
    private String labelName;

    public WithLabel(String mainData, String labelName) {
        this.mainData = mainData;
        this.contactId = -1;
        this.labelId = getCustomLabelId();
        this.labelName = labelName;
    }
    
    public WithLabel(Context ctx, String mainData, int labelId) {
        this.mainData = mainData;
        this.contactId = -1;
        this.labelId = isValidLabel(labelId) ? labelId : getDefaultLabelId();
        this.labelName = getLabelNameResId(ctx, labelId);
    }


    /**
     * <p>
     * Used to create WithLabel objects with default label
     * </p>
     *
     * @param ctx      context
     * @param mainData main data from this object , e.g. phone number , email
     */
    public WithLabel(Context ctx, String mainData) {
        this.mainData = mainData;
        this.contactId = -1;
        this.labelId = getDefaultLabelId();
        this.labelName = getLabelNameResId(ctx, labelId);
    }


    /**
     * Gets label resource by id
     *
     * @param id id of this label
     * @return string id of this label
     */
    protected abstract String getLabelNameResId(Context ctx, int id);

    protected abstract int getDefaultLabelId();

    protected abstract boolean isValidLabel(int id);

    public abstract int getCustomLabelId();


    public WithLabel() {
    }

    public String getMainData() {
        return mainData;
    }

    public int getContactId() {
        return contactId;
    }

    public int getLabelId() {
        return labelId;
    }

    public String getLabelName() {
        return labelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WithLabel withLabel = (WithLabel) o;

        if (labelId != withLabel.labelId) return false;
        if (!mainData.equals(withLabel.mainData)) return false;
        return labelName.equals(withLabel.labelName);
    }


    public WithLabel setMainData(String mainData) {
        this.mainData = mainData;
        return this;
    }

    public WithLabel setContactId(int contactId) {
        this.contactId = contactId;
        return this;
    }

    public WithLabel setLabelId(int labelId) {
        this.labelId = labelId;
        return this;
    }

    public WithLabel setLabelName(String labelName) {
        this.labelName = labelName;
        return this;
    }

    @Override
    public int hashCode() {
        return mainData.hashCode();
    }
}
