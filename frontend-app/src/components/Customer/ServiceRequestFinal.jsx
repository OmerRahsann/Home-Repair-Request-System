import React, { useState } from "react";
import Select from 'react-select';
import ImageSlider from "../ImageSlider";
import RequestEdit from "./RequestEdit";
import {AiFillEdit} from 'react-icons/ai'
import Map from "../Map/Map";

function ServiceRequestFinal({ request }) {
    const [editMode, setEditMode] = useState(false);
    const [editedRequest, setEditedRequest] = useState({ ...request });


    const handleEditClick = () => {
        setEditMode(true);
    };

    const handleSaveClick = () => {
        // Handle saving the edited request data here, you can make a PUT request.
        // Then, set editMode to false and update the editedRequest.

        // Example:
        // axios.put(`http://localhost:8080/api/customer/service_request/${request.id}`, editedRequest).then(response => {
        //     setEditedRequest(response.data);
        //     setEditMode(false);
        // });

        setEditMode(false);
    };

    const handleChange = (event) => {
        const { name, value } = event.target;
        setEditedRequest({ ...editedRequest, [name]: value });
    };

    return (
        <div>
            <div>
                {editMode ? (
                    <>
                       <RequestEdit request={request}/>
                        {/* Render other editable fields similarly */}
                    </>
                ) : (
                    <>
                        <div>
                            <ImageSlider images={request.pictures} />
                            <div className='p-2 flex flex-col'>
                                <div className="flex flex-row justify-between">
                                    <h1 className="text-[2.5vh] font-semibold">{request.title}</h1>
                                    <AiFillEdit onClick={handleEditClick}/>
                                </div>
                                <h2 className='text-[1.5vh]'>{request.address}</h2>
                                <p>{request.description}</p>
                                <h2 className='text-[1vh]'>Price Range: {request.dollars}</h2>
                            </div>
                            <div>
                            <Map address={request.address} />
                        </div>
                        </div>
                        {/* Render other non-editable fields */}
                    </>
                )}
            </div>
        </div>
    );
}

export default ServiceRequestFinal;
