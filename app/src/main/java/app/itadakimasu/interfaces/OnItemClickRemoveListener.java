package app.itadakimasu.interfaces;
/**
 * Interface used to implement the removing feature on recipe in the fragments.
 */
public interface OnItemClickRemoveListener {
    /**
     * Method that will be used to implement the removing feature on every item on an adapter.
     * @param recipePosition - the position of the item selected.
     */
    void onItemClickRemove(int recipePosition);
}
