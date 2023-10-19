import React, { useState, useEffect, createRef } from 'react';

function RequestDetails({ request }) {


    return (
        <div className="shadow-md border-2 border-gray-400 rounded-md">
            <img className="w-full" src='https://imageio.forbes.com/specials-images/imageserve/64dba09a75d499fc7888000c/0x0.jpg?format=jpg&height=900&width=1600&fit=bounds'></img>
            <div className='bg-custom-grain p-2 flex flex-col'>
            <h1 className="text-[2vh] font-semibold">{request.title}</h1>
            <h2 className='text-[1vh]'>{request.address}</h2>
            <h2 className='text-[1vh]'>Price Range: {request.dollars}</h2>
        

            </div>
        </div>

    );
}

export default RequestDetails;