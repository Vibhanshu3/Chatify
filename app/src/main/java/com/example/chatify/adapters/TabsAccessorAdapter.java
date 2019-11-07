package com.example.chatify.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.chatify.Fragments.ChatsFragment;
import com.example.chatify.Fragments.ContactsFragment;
import com.example.chatify.Fragments.RequestFragment;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

//            case 1:
//                GroupsFragment groupsFragment = new GroupsFragment();
//                return groupsFragment;

            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

             default:
                 return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Chats";

//            case 1:
//                return "Groups";

            case 1:
                return "Contacts";

            case 2:
                return "Request";



            default:
                return null;

        }
    }
}
