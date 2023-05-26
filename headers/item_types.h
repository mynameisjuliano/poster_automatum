#define TYPE_HIGH_QUALITY_BAG "high_quality_bag"
#define TYPE_LOW_QUALITY_BAG  "low_quality_bag"
#define SPEAKER                "speaker"
#define HOUSEHOLD_ITEM         "household_item"
#define SMALL_HOUSEHOLD_ITEM   "small_household_item"
#define FOOTWEAR               "footwear"
#define PHONE                  "phone"
#define CLOTHES                "clothes"

#define MESSAGE_SIZE           (int) 1024 * 2.5

enum item_type {
	default_type,         // 0
	bag_high_quality,     // 1
	bag_low_quality,      // 2
	phone,                // 3
	speaker,              // 4
	household_item,       // 5
	household_item_small, // 6
	footwear,             // 7
	clothes               // 8
};

struct supplier {
	char* id;
	char* name;
	enum item_type type;
}

anna_cai_bags = {"100063763044968", "👜 Anna Cai Bags", bag_low_quality},
bea_tan_bags = {"100057227688584", "👜 Bea Tan Reyes Bags", bag_high_quality},
kk_store = {"100057183110957", "🏘️ KK's Online Shop", household_item_small},
dianne_sotto = {"100064897175497", "🟣 Dianne Sotto", household_item};
