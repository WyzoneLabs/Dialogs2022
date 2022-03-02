package contacts.interfaces;


import java.util.List;

import contacts.entity.ContactData;

/**
 * <p>
 * Abstract class for filtering contacts with fields that are lists.
 * </p>
 *
 * @param <T> type of container of target data
 * @param <V> type of field you want to filter
 */

public abstract class ListFilter<T, V> extends BaseFilter<T, V> {

    /**
     * User to get container of filterable data
     *
     * @param contact contact object where from to get data
     * @return list with target data
     */
    protected abstract List<T> getFilterContainer(ContactData contact);

    @Override
    public boolean passedFilter(ContactData contact) {
        for (T t : getFilterContainer(contact)) {
            if (getFilterCondition(getFilterData(t), getFilterPattern()))
                return true;
        }
        return false;
    }
}
