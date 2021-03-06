package contacts.entity;

import android.content.Context;
import android.provider.ContactsContract;

import contacts.interfaces.WithLabel;

public class PhoneNumber extends WithLabel {

    private boolean isPrimary;

    public PhoneNumber(String mainData, String labelName) {
        super(mainData, labelName);
    }

    public PhoneNumber(Context ctx, String mainData, int labelId) {
        super(ctx, mainData, labelId);
    }

    public PhoneNumber(Context ctx, String mainData) {
        super(ctx, mainData);
    }

    @Override
    protected String getLabelNameResId(Context ctx, int id) {
        return ctx.getString(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(id));
    }

    @Override
    protected int getDefaultLabelId() {
        return TYPE_HOME;
    }

    @Override
    protected boolean isValidLabel(int id) {
        return id >= 0 && id <= 20;
    }

    @Override
    public int getCustomLabelId() {
        return 0;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public PhoneNumber setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        if (!super.equals(o)) return false;

        PhoneNumber that = (PhoneNumber) o;

        return isPrimary == that.isPrimary;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isPrimary ? 1 : 0);
        return result;
    }

    public static final int TYPE_HOME = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_WORK = 3;
    public static final int TYPE_FAX_WORK = 4;
    public static final int TYPE_FAX_HOME = 5;
    public static final int TYPE_PAGER = 6;
    public static final int TYPE_OTHER = 7;
    public static final int TYPE_CALLBACK = 8;
    public static final int TYPE_CAR = 9;
    public static final int TYPE_COMPANY_MAIN = 10;
    public static final int TYPE_ISDN = 11;
    public static final int TYPE_MAIN = 12;
    public static final int TYPE_OTHER_FAX = 13;
    public static final int TYPE_RADIO = 14;
    public static final int TYPE_TELEX = 15;
    public static final int TYPE_TTY_TDD = 16;
    public static final int TYPE_WORK_MOBILE = 17;
    public static final int TYPE_WORK_PAGER = 18;
    public static final int TYPE_ASSISTANT = 19;
    public static final int TYPE_MMS = 20;
}
