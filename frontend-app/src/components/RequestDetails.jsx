import React from 'react';
import ImageSlider from './ImageSlider';

function RequestDetails({ request }) {
    return (
        <div className="shadow-md border-2 border-gray-400 rounded-md">
            <ImageSlider images={request.pictures} />
            <div className='bg-custom-grain p-2 flex flex-col'>
                <h1 className="text-[2.5vh] font-semibold">{request.title}</h1>
                <h2 className='text-[1.5vh]'>{request.address}</h2>
                <h2 className='text-[1vh]'>Price Range: {request.dollars}</h2>
            </div>
        </div>
    );
}

export default RequestDetails;
