
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import PaymentManager from "./components/PaymentManager"

import RoomManager from "./components/RoomManager"
import ReviewManager from "./components/ReviewManager"

import ReservationManager from "./components/ReservationManager"

import RoomView from "./components/RoomView"
import MessageManager from "./components/MessageManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Payment',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/Room',
                name: 'RoomManager',
                component: RoomManager
            },
            {
                path: '/Review',
                name: 'ReviewManager',
                component: ReviewManager
            },

            {
                path: '/Reservation',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/RoomView',
                name: 'RoomView',
                component: RoomView
            },
            {
                path: '/Message',
                name: 'MessageManager',
                component: MessageManager
            },



    ]
})
