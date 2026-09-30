def filter_components(
        components: list[dict],
        min_area: int = 50,    #Mudar valor depois
        max_area: int = 500   #Mudar valor depois
) -> list[dict]:
    valid_components = []


    for component in components:
        area = component['area']
        if min_area <= area <= max_area:
            valid_components.append(component)

    return valid_components