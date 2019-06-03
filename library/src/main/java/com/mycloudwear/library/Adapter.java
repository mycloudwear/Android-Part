package com.mycloudwear.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
public class Adapter extends RecyclerView.Adapter<VH> {

    private ArrayList<Country> selectedCountries = new ArrayList<>();
    private final LayoutInflater inflater;
    private OnPick onPick = null;
    private final Context context;
    public Adapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }

    /**
     * This function is used to set a selected country list.
     * @param selectedCountries the ArrayList which contains selected countries.
     */
    public void setSelectedCountries(ArrayList<Country> selectedCountries) {
        this.selectedCountries = selectedCountries;
        notifyDataSetChanged();
    }

    /**
     * This function is used to pick a country.
     * @param onPick an object of the OnPick.
     */
    public void setOnPick(OnPick onPick) {
        this.onPick = onPick;
    }

    /**
     * This function is used to create a view holder.
     * @param parent the object of ViewGroup.
     * @param viewType the type of the view.
     * @return a object of VH class.
     */
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(inflater.inflate(R.layout.item_country, parent, false));
    }

    private int itemHeight = -1;

    /**
     * This function bind each country code at specific position.
     * @param holder the object of the VH class.
     * @param position the position of the given country at the layout.
     */
    @Override
    public void onBindViewHolder(VH holder, int position) {
        final Country country = selectedCountries.get(position);
        holder.ivFlag.setImageResource(country.flag);
        holder.tvName.setText(country.name);
        holder.tvCode.setText("+" + country.code);
        if(itemHeight != -1) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = itemHeight;
            holder.itemView.setLayoutParams(params);
        }
        holder.itemView.setOnClickListener(v -> {
            if(onPick != null) onPick.onPick(country);
        });
    }

    /**
     * This function could get the size of the selected country list.
     * @return the size of the list.
     */
    @Override
    public int getItemCount() {
        return selectedCountries.size();
    }

}
