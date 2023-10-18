import React, { useState, useEffect, createRef } from 'react';
import Select from 'react-select';
import RequestDetails from './RequestDetails'

function RequestList({ onSearch }) {


    const requests = [
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" },
        { name: "Gutter" }
    ]

    const category = [
        { value: "plumbing", label: "Plumbing" },
        { value: "yardwork", label: "Yardwork" },
        { value: "roofing", label: "Roofing" },
    ];

    const priceRange = [
        { value: "500", label: "$0-500" },
        { value: "1000", label: "500-$1000" },
        { value: "2500", label: "$1000-10,000" },
    ];

    const [categoryChange, setCategoryChange] = useState([]);
    const [priceRangeChange, setPriceRangeChange] = useState([]);

    function handleCategoryChage(data) {
        setCategoryChange(data);
    }

    function handlePriceSelect(data) {
        setPriceRangeChange(data);
    }

    return (
        <div className='p-25 '>
            <h1 className='font-bold text-lg text-center bg-custom-tan p-5'>Requests Near You!</h1>

            <>

                <div className='flex flex-row justify-between pt-10 pb-10'>
                    <Select
                        options={category}
                        placeholder="Category"
                        value={categoryChange}
                        onChange={handleCategoryChage}
                        isSearchable={true}
                        isMulti
                        className="bg-custom-gray"
                    />

                    <Select
                        options={priceRange}
                        placeholder="Price Range"
                        value={priceRangeChange}
                        onChange={handlePriceSelect}
                        isSearchable={true}
                        isMulti
                        className="bg-custom-gray"
                    />

                </div>






                <div className="h-[65vh] overflow-y-auto ">
                    <div className="flex flex-wrap gap-4">
                        {requests?.map((request, i) => (
                            <div className="w-full p-2 " key={i}>
                                <RequestDetails request={request} />
                            </div>
                        ))}
                    </div>
                </div>

            </>

        </div>
    );
}

export default RequestList;