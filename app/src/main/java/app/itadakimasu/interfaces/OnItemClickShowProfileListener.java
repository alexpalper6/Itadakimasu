package app.itadakimasu.interfaces;

/**
 * Interface used to let the user visit others users profiles on a list of recipes.
 */
public interface OnItemClickShowProfileListener {
    /**
     * Method to implement the interface functionality.
     * @param recipePosition - the recipe's position.
     */
    void onItemShowProfile(int recipePosition);
}
