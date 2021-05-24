package cai.lib.kvstore

import cai.lib.kvstore.annotations.FieldIgnore
import cai.lib.kvstore.annotations.FieldRename
import cai.lib.kvstore.annotations.StoreRename
import com.google.gson.annotations.SerializedName

@StoreRename(value = "AppStore")
class AppStore : BaseStore() {

    @SerializedName("app_value")
    var value = "test"

    @FieldRename("app_name")
    var name = "test"

    //忽略该元素
    @FieldIgnore
    var str = "test"
}