package ASLENIX.pharmacy.demo.utils;


import ASLENIX.pharmacy.demo.model.User;

import java.io.IOException;
import java.util.List;

public class ExportUsers {

    private List<User> userList ;

    ExportUsers (List<User> userList){
        this.userList = userList;

    }

    public byte[] export() throws IOException {

        return new byte[5];
    }



}
