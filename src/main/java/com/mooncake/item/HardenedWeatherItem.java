package com.mooncake.item;

import com.mooncake.component.MooncakeWeather;

/** Hardened gear / armor that encodes oxidation + wax in the item type. */
public interface HardenedWeatherItem {
    MooncakeWeather weather();
}
