import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import image from "../../Pictures/banner.jpeg"

function ServiceRequestModal({isVisible, onClose, children}) {
    const [serviceRequestModel, setServiceRequestModel] = useState({
        title: '',
        description: '',
        dollars: 0,
    });

    const [serviceRequests, setServiceRequests] = useState([]);
    const [imageLoaded, setImageLoaded] = useState(false);
    const [isFormOpen, setIsFormOpen] = useState(false);

      const handleClose = (e) => {
        if(e.target.id === 'wrapper') onClose();
      }

    if(!isVisible) return null ;
    return (
      
        <div className="fixed inset-0 bg-black bg-opacity-25 backdrop-blur-sm items-center flex justify-center" id="wrapper" onClick={handleClose}>
            <div className='w-[600px] flex flex-col'>
                <button className='text-white text-xl place-self-end' onClick={() => onClose()}>X</button>
                <div className='bg-white p-2'>
                    {children}
                </div>

            </div>
            
        </div> 

    );
}

export default ServiceRequestModal;
