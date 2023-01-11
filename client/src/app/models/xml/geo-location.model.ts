export class GeoLocationPoint {
  pointLongitude: number;
  pointLatitude: number;

  constructor(long: number, lat: number) {
    this.pointLongitude = long;
    this.pointLatitude = lat;
  }
}

export class GeoLocationBox {
  northBoundLatitude: number;
  southBoundLatitude: number;
  westBoundLongitude: number;
  eastBoundLongitude: number;

  constructor(n: number, s: number, w: number, e: number) {
    this.northBoundLatitude = n;
    this.southBoundLatitude = s;
    this.westBoundLongitude = w;
    this.eastBoundLongitude = e;
  }
}

export class GeoLocationPolygon {
  polygonPoint: GeoLocationPoint[];
  inPolygonPoint : GeoLocationPoint;

  constructor(points: GeoLocationPoint[], inPoint: GeoLocationPoint) {
    this.polygonPoint = points;
    this.inPolygonPoint = inPoint;
  }
}

export class GeoLocation {
  geoLocationPlace: string;
  geoLocationPoint: GeoLocationPoint;
  geoLocationBox: GeoLocationBox[];
  geoLocationPolygon: GeoLocationPolygon[];

  constructor(place: string, point: GeoLocationPoint, boxs: GeoLocationBox[], polygons: GeoLocationPolygon[]) {
    this.geoLocationPlace = place;
    this.geoLocationPoint = point;
    this.geoLocationBox = boxs;
    this.geoLocationPolygon = polygons;
  }
}