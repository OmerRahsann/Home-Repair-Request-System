import { useState } from "react";

function RequestView() {
    const [viewPort, setViewPort] = useState({
        latitude: 45.2,
        longitude: -75,
        width: '100vw',
       height: '100vw',
       zoom: 10

    })


    return (
        <div>
            hello
           {/* <ReactMapGL {...viewPort} mapboxAccessToken={process.env.REACT_APP_MAPBOX_TOKEN}>
            Map here
           </ReactMapGL> */}
        </div>
    )

}
  export default RequestView;