package com.mycloudwear.library;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
class VH extends RecyclerView.ViewHolder {

    TextView tvName;
    TextView tvCode;
    ImageView ivFlag;

    VH(View itemView) {
        super(itemView);
        ivFlag = (ImageView) itemView.findViewById(R.id.iv_flag);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
        tvCode = (TextView) itemView.findViewById(R.id.tv_code);
    }
}
