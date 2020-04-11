package ovh.jujulacuillere.withingstostrava.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class WithingsUser implements Parcelable {
    private String login;
    private String password;
    private int id;
    private Bitmap photo;
    private String firstName;
    private String lastName;

    public WithingsUser(String loginValue, String passwordValue) {
        super();
        this.login = loginValue;
        this.password = passwordValue;
    }

    private WithingsUser(Parcel in) {
        this.login = in.readString();
        this.password = in.readString();
        this.id = in.readInt();
        this.photo = in.readParcelable(Bitmap.class.getClassLoader());
        this.firstName = in.readString();
        this.lastName = in.readString();
    }

    public static final Creator<WithingsUser> CREATOR = new Creator<WithingsUser>() {
        @Override
        public WithingsUser createFromParcel(Parcel in) {
            return new WithingsUser(in);
        }

        @Override
        public WithingsUser[] newArray(int size) {
            return new WithingsUser[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photoValue) {
        this.photo = photoValue;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.login);
        dest.writeString(this.password);
        dest.writeInt(this.id);
        dest.writeParcelable(this.photo, flags);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
    }
}
