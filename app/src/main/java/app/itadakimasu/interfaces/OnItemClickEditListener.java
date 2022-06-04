package app.itadakimasu.interfaces;

/**
 * Interface used to implement the editing feature on recipes in the fragments.
 */
public interface OnItemClickEditListener {
    /**
     * Method that will be used to implement the edit feature on every recipe on an adapter.
     * @param recipePosition - the position of the item selected.
     */
    void onItemClickEdit(int recipePosition);
}
