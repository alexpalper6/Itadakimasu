package app.itadakimasu.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Model class for ingredients documents on its firebase collection (a recipe sub-collection).
 */
public class Ingredient implements Parcelable {
    private int ingredientPosition;
    private String ingredientDescription;

    /**
     * Creates an ingredient with its description and its position on the list.
     * @param ingredientPosition - the position of the ingredient on the list.
     * @param ingredientDescription - its description.
     */
    public Ingredient(int ingredientPosition, String ingredientDescription) {
        this.ingredientPosition = ingredientPosition;
        this.ingredientDescription = ingredientDescription;
    }

    /**
     * Creates an ingredient with its description.
     * @param ingredientDescription - the ingredient description.
     */
    public Ingredient(String ingredientDescription) {
        this.ingredientPosition = 0;
        this.ingredientDescription = ingredientDescription;
    }

    /**
     * Default constructor that is required for firebase.
     */
    public Ingredient() {

    }


    public int getIngredientPosition() {
        return ingredientPosition;
    }

    public String getIngredientDescription() {
        return ingredientDescription;
    }

    public void setIngredientPosition(int ingredientPosition) {
        this.ingredientPosition = ingredientPosition;
    }

    public void setIngredientDescription(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
    }

    // Auto generated parcelable //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(ingredientDescription, that.ingredientDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientDescription);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ingredientPosition);
        dest.writeString(this.ingredientDescription);
    }

    public void readFromParcel(Parcel source) {
        this.ingredientPosition = source.readInt();
        this.ingredientDescription = source.readString();
    }

    protected Ingredient(Parcel in) {
        this.ingredientPosition = in.readInt();
        this.ingredientDescription = in.readString();
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel source) {
            return new Ingredient(source);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}
