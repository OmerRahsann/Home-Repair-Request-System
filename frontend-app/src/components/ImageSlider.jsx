import React, { useState, useEffect } from 'react';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css';

function ImageSlider({ images }) {
  const [currentIndex, setCurrentIndex] = useState(0);

  const [isHovered, setIsHovered] = useState(false);

  const handleImageChange = (index) => {
    setCurrentIndex(index);
  };

  const goNext = () => {
    const newIndex = (currentIndex + 1) % images.length;
    setCurrentIndex(newIndex);
  };

  const goPrev = () => {
    const newIndex = (currentIndex - 1 + images.length) % images.length;
    setCurrentIndex(newIndex);
  };

  useEffect(() => {
    let interval;

    if (!isHovered) {
      interval = setInterval(() => {
        // Change the current index automatically
        const newIndex = (currentIndex + 1) % images.length;
        setCurrentIndex(newIndex);
      }, 10000); // Change the image every 5 seconds (adjust this time as needed)
    }

    return () => clearInterval(interval);
  }, [currentIndex, images, isHovered]);


  return (
    <div>
      <Carousel
        showThumbs={false}
        selectedItem={currentIndex}
        onChange={handleImageChange}
        renderArrowPrev={(onClickHandler, hasPrev, label) =>
            hasPrev && (
              <button onClick={onClickHandler} className="absolute left-0 top-1/2 transform -translate-y-1/2 bg-black">
                
              </button>
            )
          }
          renderArrowNext={(onClickHandler, hasNext, label) =>
            hasNext && (
              <button onClick={onClickHandler} className="absolute right-0 top-1/2 transform -translate-y-1/2">
                
              </button>
            )
          }
      >
        {images.map((imageUrl, index) => (
          <div key={index} className=''>
            <img src={`http://localhost:8080/image/${imageUrl}`} alt={`Image ${index}`} className='object-cover h-[40vh] w-[80vh]'/>
          </div>
        ))}
      </Carousel>
    </div>
  );
}

export default ImageSlider;
