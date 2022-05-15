package app.itadakimasu.ui.recipeCreation;
/**
 * Interface used to implement the removing feature on items in the fragments.
 */
public interface OnItemClickRemoveListener {
    /**
     * Method that will be used to implement the removing feature on every item on an adapter.
     * @param itemPosition - the position of the item selected.
     */
    void onItemClickRemove(int itemPosition);
}
