package app.itadakimasu.interfaces;

/**
 * Interface used to implement the editing feature on items in the fragments.
 */
public interface OnItemClickEditListener {
    /**
     * Method that will be used to implement the edit feature on every item on an adapter.
     * @param itemPosition - the position of the item selected.
     */
    void onItemClickEdit(int itemPosition);
}
