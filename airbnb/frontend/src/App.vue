<template>
    <v-app id="inspire">
        <div>
            <v-app-bar app clipped-left flat>
                <v-toolbar-title>
                    <span class="second-word font uppercase"
                        >airbnb</span
                    >
                </v-toolbar-title>
                <v-spacer></v-spacer>
            </v-app-bar>

            <v-navigation-drawer app clipped flat>
                <v-list>
                    <v-list-item
                        class="px-2"
                        v-for="component in components"
                        :key="component"
                        :to="`/${component}`"
                        color="deep-purple lighten-2"
                    >
                        {{ component }}
                    </v-list-item>
                </v-list>
            </v-navigation-drawer>
        </div>

        <v-main>
            <v-container class="py-8 px-6" fluid>
                <router-view></router-view>
            </v-container>
        </v-main>
    </v-app>
</template>

<script>
export default {
    name: "App",

    components: {},

    data: () => ({
        useComponent: "",
        drawer: true,
        components: []
    }),

    mounted() {
        var me = this;
        me.components = this.$ManagerLists;
    }
};
</script>
<style>
</style>
