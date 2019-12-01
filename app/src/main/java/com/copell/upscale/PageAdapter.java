package com.copell.upscale;



import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


public class PageAdapter extends FragmentStatePagerAdapter {

    String[] tabArray = new String[]{"Inventory", "Sales"};
    private int numberOfTabs;


    public PageAdapter(FragmentManager fm) {
        super(fm);
        this.numberOfTabs = tabArray.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                InventoryFragment studentFragment = new InventoryFragment();
                return studentFragment;
            case  1:
                DiscountFragment notificationFragment = new DiscountFragment();
                return notificationFragment;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabArray[position];
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
