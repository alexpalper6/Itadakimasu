package app.itadakimasu.interfaces;

/**
 * Interface used to implement the adding to favourite feature on recipes in the fragments.
 */
public interface OnItemClickAddFavListener {
    /**
     * Method that implements the adding to favourite functionality.
     * @param recipePosition - the recipe's position.
     */
    void onItemAddFavListener(int recipePosition);
}
